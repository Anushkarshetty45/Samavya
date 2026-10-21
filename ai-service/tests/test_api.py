import json
from types import SimpleNamespace
from typing import Any
from unittest.mock import AsyncMock, patch

import pytest
import httpx
from fastapi import FastAPI
from fastapi.testclient import TestClient
from openai import APIStatusError, APITimeoutError
from pydantic import SecretStr

from samavya_ai.config import Settings
from samavya_ai.main import create_app
from samavya_ai.nebius import (
    ModelTimeoutError,
    ModelUnavailableError,
    NebiusIntentInterpreter,
)


class FakeCompletion:
    def __init__(self, response: Any) -> None:
        self.response = response

    async def __call__(self, _system_prompt: str, _user_content: str) -> str:
        if isinstance(self.response, Exception):
            raise self.response
        if isinstance(self.response, str):
            return self.response
        return json.dumps(self.response)


def configured_settings() -> Settings:
    return Settings(
        nebius_api_key=SecretStr("test-only-key"),
        nebius_model="test/nemotron",
    )


def app_with_response(response: Any) -> FastAPI:
    settings = configured_settings()
    interpreter = NebiusIntentInterpreter(
        settings,
        completion_function=FakeCompletion(response),
    )
    return create_app(settings=settings, interpreter=interpreter)


def model_intent(
    intent: str,
    entities: dict[str, Any] | None = None,
    intended_action: str | None = None,
) -> dict[str, Any]:
    return {
        "intent": intent,
        "intended_action": intended_action,
        "entities": entities or {},
    }


def test_health_and_readiness_without_configuration() -> None:
    client = TestClient(create_app(settings=Settings()))

    assert client.get("/health").status_code == 200
    readiness = client.get("/ready")
    assert readiness.status_code == 503
    assert readiness.json()["detail"]["code"] == "nebius_not_configured"


def test_interpret_rejects_missing_configuration_without_calling_model() -> None:
    client = TestClient(create_app(settings=Settings()))

    response = client.post("/ai/interpret", json={"message": "Find a plumber"})

    assert response.status_code == 503
    assert response.json()["detail"]["code"] == "nebius_not_configured"


def test_ready_when_key_and_model_are_configured() -> None:
    client = TestClient(app_with_response(model_intent("GENERAL_QUESTION")))

    assert client.get("/ready").status_code == 200


def test_nebius_client_uses_chat_completions_contract() -> None:
    settings = configured_settings()
    sdk_client = SimpleNamespace(
        chat=SimpleNamespace(
            completions=SimpleNamespace(
                create=AsyncMock(
                    return_value=SimpleNamespace(
                        choices=[
                            SimpleNamespace(
                                message=SimpleNamespace(
                                    content=json.dumps(
                                        model_intent("GENERAL_QUESTION")
                                    )
                                )
                            )
                        ]
                    )
                )
            )
        ),
        close=AsyncMock(),
    )
    interpreter = NebiusIntentInterpreter(settings)

    with patch("samavya_ai.nebius.AsyncOpenAI", return_value=sdk_client) as factory:
        with TestClient(create_app(settings=settings, interpreter=interpreter)) as client:
            response = client.post(
                "/ai/interpret",
                json={"message": "What can you help with?"},
            )

    assert response.status_code == 200
    factory.assert_called_once_with(
        api_key="test-only-key",
        base_url="https://api.tokenfactory.nebius.com/v1/",
        timeout=20.0,
        max_retries=0,
    )
    call = sdk_client.chat.completions.create.call_args.kwargs
    assert call["model"] == "test/nemotron"
    assert "response_format" not in call
    assert [message["role"] for message in call["messages"]] == ["system", "user"]
    assert "JSON object matching the supplied schema" in call["messages"][0]["content"]
    assert json.loads(call["messages"][1]["content"]) == {
        "message": "What can you help with?",
        "conversation_context": None,
    }
    sdk_client.close.assert_awaited_once()


@pytest.mark.parametrize(
    ("sdk_error", "expected_status", "expected_code"),
    [
        (
            APITimeoutError(
                httpx.Request(
                    "POST",
                    "https://api.tokenfactory.nebius.com/v1/chat/completions",
                )
            ),
            504,
            "nebius_timeout",
        ),
        (
            APIStatusError(
                "upstream failure",
                response=httpx.Response(
                    500,
                    request=httpx.Request(
                        "POST",
                        "https://api.tokenfactory.nebius.com/v1/chat/completions",
                    ),
                ),
                body=None,
            ),
            502,
            "nebius_upstream_error",
        ),
    ],
)
def test_maps_openai_sdk_upstream_errors(
    sdk_error: Exception,
    expected_status: int,
    expected_code: str,
) -> None:
    settings = configured_settings()
    sdk_client = SimpleNamespace(
        chat=SimpleNamespace(
            completions=SimpleNamespace(create=AsyncMock(side_effect=sdk_error))
        ),
        close=AsyncMock(),
    )
    interpreter = NebiusIntentInterpreter(settings)

    with patch("samavya_ai.nebius.AsyncOpenAI", return_value=sdk_client):
        with TestClient(create_app(settings=settings, interpreter=interpreter)) as client:
            response = client.post(
                "/ai/interpret",
                json={"message": "Find a plumber"},
            )

    assert response.status_code == expected_status
    assert response.json()["detail"]["code"] == expected_code


@pytest.mark.parametrize(
    ("payload", "expected_intent", "expected_category", "expected_response"),
    [
        (
            model_intent(
                "SEARCH_SERVICES",
                {
                    "service_category": "plumber",
                    "community_reference": "Riverbend",
                },
            ),
            "SEARCH_SERVICES",
            "plumber",
            "I interpreted this as a service search. I have not queried marketplace listings.",
        ),
        (
            model_intent(
                "CREATE_REQUIREMENT",
                {
                    "requirement_type": "SERVICE",
                    "requirement_title": "Fix kitchen sink",
                    "service_category": "plumbing",
                    "quantity": 1,
                    "budget": 80,
                    "community_reference": "Riverbend",
                },
            ),
            "CREATE_REQUIREMENT",
            "plumbing",
            "I interpreted this as a draft community requirement. It has not been submitted.",
        ),
        (
            model_intent("GENERAL_QUESTION"),
            "GENERAL_QUESTION",
            None,
            "I understood this as a general question. This service extracts intent only and does not access marketplace data.",
        ),
    ],
)
def test_interprets_each_non_clarification_intent(
    payload: dict[str, Any],
    expected_intent: str,
    expected_category: str | None,
    expected_response: str,
) -> None:
    client = TestClient(app_with_response(payload))

    response = client.post(
        "/ai/interpret",
        json={"message": "User request", "conversation_context": "Riverbend"},
    )

    assert response.status_code == 200
    result = response.json()
    assert result["intent"] == expected_intent
    assert result["entities"]["service_category"] == expected_category
    assert result["response"] == expected_response
    assert result["clarification_question"] is None


def test_extracts_needs_clarification_and_lists_missing_information() -> None:
    client = TestClient(
        app_with_response(
            model_intent(
                "SEARCH_SERVICES",
                {"service_category": "plumber"},
            )
        )
    )

    response = client.post("/ai/interpret", json={"message": "Find a plumber"})

    assert response.status_code == 200
    result = response.json()
    assert result["intent"] == "NEEDS_CLARIFICATION"
    assert result["target_intent"] == "SEARCH_SERVICES"
    assert result["missing_information"] == ["community_reference"]
    assert result["clarification_question"] == (
        "Please provide the Samavya community."
    )
    assert result["response"] is None


def test_handles_model_requested_clarification() -> None:
    client = TestClient(
        app_with_response(
            model_intent(
                "NEEDS_CLARIFICATION",
                intended_action="CREATE_REQUIREMENT",
            )
        )
    )

    response = client.post("/ai/interpret", json={"message": "I need something"})

    assert response.status_code == 200
    result = response.json()
    assert result["intent"] == "NEEDS_CLARIFICATION"
    assert result["missing_information"] == [
        "requirement_type",
        "requirement_title",
        "community_reference",
    ]
    assert "requirement" in result["clarification_question"]


def test_ambiguous_intent_requests_clarification() -> None:
    client = TestClient(
        app_with_response(model_intent("NEEDS_CLARIFICATION"))
    )

    response = client.post("/ai/interpret", json={"message": "Can you help?"})

    assert response.status_code == 200
    result = response.json()
    assert result["intent"] == "NEEDS_CLARIFICATION"
    assert result["missing_information"] == ["intent"]
    assert "search for a service" in result["clarification_question"]


def test_service_requirement_requires_a_category() -> None:
    client = TestClient(
        app_with_response(
            model_intent(
                "CREATE_REQUIREMENT",
                {
                    "requirement_type": "SERVICE",
                    "requirement_title": "Fix sink",
                    "community_reference": "Riverbend",
                },
            )
        )
    )

    response = client.post(
        "/ai/interpret",
        json={"message": "I need a service requirement"},
    )

    assert response.status_code == 200
    result = response.json()
    assert result["intent"] == "NEEDS_CLARIFICATION"
    assert result["missing_information"] == ["service_category"]


@pytest.mark.parametrize(
    "model_output",
    [
        "not json",
        {"intent": "UNKNOWN", "entities": {}},
        {
            "intent": "SEARCH_SERVICES",
            "entities": {"community_id": 12},
        },
        {
            "intent": "CREATE_REQUIREMENT",
            "entities": {"requirement_type": "INVALID"},
        },
        {
            "intent": "CREATE_REQUIREMENT",
            "entities": {"quantity": -1},
        },
    ],
)
def test_rejects_malformed_or_invalid_model_output(model_output: Any) -> None:
    client = TestClient(app_with_response(model_output))

    response = client.post("/ai/interpret", json={"message": "Any request"})

    assert response.status_code == 502
    assert response.json()["detail"]["code"] == "invalid_model_response"


@pytest.mark.parametrize(
    ("failure", "expected_status", "expected_code"),
    [
        (ModelTimeoutError(), 504, "nebius_timeout"),
        (ModelUnavailableError(), 502, "nebius_upstream_error"),
    ],
)
def test_handles_upstream_timeout_and_errors(
    failure: Exception,
    expected_status: int,
    expected_code: str,
) -> None:
    client = TestClient(app_with_response(failure))

    response = client.post("/ai/interpret", json={"message": "Find a plumber"})

    assert response.status_code == expected_status
    assert response.json()["detail"]["code"] == expected_code


@pytest.mark.parametrize(
    "body",
    [
        {"message": ""},
        {"message": "   "},
        {"message": "x" * 4001},
        {"message": "Find a plumber", "user_id": 5},
    ],
)
def test_validates_interpret_request(body: dict[str, Any]) -> None:
    client = TestClient(app_with_response(model_intent("GENERAL_QUESTION")))

    assert client.post("/ai/interpret", json=body).status_code == 422
