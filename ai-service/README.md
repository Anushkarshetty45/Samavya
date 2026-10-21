# Samavya AI service

This is a separate FastAPI service for extracting conversational intent using
Nebius Token Factory and an NVIDIA Nemotron model. It does not connect to
PostgreSQL or call Spring Boot. It cannot search listings, create requirements,
or perform other marketplace actions.

## Nebius API configuration

The client uses Nebius's OpenAI-compatible Chat Completions API:

- Base URL: `https://api.tokenfactory.nebius.com/v1/`
- Chat Completions: `POST /chat/completions`
- Authentication: the OpenAI client sends the configured API key as a bearer
  token in the `Authorization` header.
- Request: `model` plus `messages` containing `role` and `content`.
- Response: assistant content is read from `choices[0].message.content`.

The default, `nvidia/NVIDIA-Nemotron-3-Nano-30B-A3B`, is listed as an active
text-to-text model in Nebius's public Token Factory catalog. The same catalog
also lists `nvidia/Nemotron-3_5-Lightning`,
`nvidia/nemotron-3-super-120b-a12b`, and
`nvidia/Nemotron-3-Ultra-550b-a55b`. Nano is the configured default; the catalog
describes it as a compact model for reasoning, chat, and coding, not as a model
specifically benchmarked for Samavya intent extraction. Confirm that the chosen
model is enabled for your project and region; account-specific access may vary.
Nebius documents the authenticated `GET /v1/models` endpoint for checking
currently available IDs. The API key must remain in the server environment and
must never be sent by a browser.

Sources:

- [Nebius Quickstart](https://docs.tokenfactory.nebius.com/quickstart)
- [Nebius API introduction and authentication](https://docs.tokenfactory.nebius.com/api-reference/introduction)
- [Nebius text generation request/response example](https://docs.tokenfactory.nebius.com/api-reference/examples/text-generation)
- [Nebius list-models API](https://docs.tokenfactory.nebius.com/api-reference/models/list-models)
- [Nebius Token Factory public model catalog](https://tokenfactory.nebius.com/model-catalog.md)
- [Nebius machine-readable public model catalog](https://tokenfactory.nebius.com/api/public/models_info)
- [Nebius structured JSON output](https://docs.tokenfactory.nebius.com/ai-models-inference/json)
- [Nebius API OpenAPI definition](https://api.tokenfactory.nebius.com/openapi.json)

The model is prompted with a JSON schema, then its content is independently
parsed and validated against Pydantic schemas. The request deliberately avoids
Nebius's optional JSON-mode parameter because the public catalog does not
document JSON-mode support for the selected Nemotron model. Model output is
untrusted. The API returns only extracted intent fields and a bounded,
deterministic response or clarification question. It never treats an intent
as a completed marketplace action.

## Local setup

Python 3.11 or newer is required. From the repository root, create the local
environment and install the service with its test dependencies:

```powershell
Set-Location ai-service
py -3.12 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install -e ".[test]"
Copy-Item .env.example .env
```

Edit `.env` and set `NEBIUS_API_KEY` and a currently available
`NEBIUS_MODEL`. Keep `.env` private; it is ignored by Git. Alternatively,
provide these variables through the process environment or a secrets manager.

Start the service:

```powershell
python -m uvicorn samavya_ai.main:app --reload
```

Run automated tests (they use mocked model responses and do not require a
Nebius account, API key, or paid inference):

```powershell
python -m pytest
```

Equivalent setup on macOS/Linux:

```bash
cd ai-service
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
python -m pip install -e ".[test]"
cp .env.example .env
python -m uvicorn samavya_ai.main:app --reload
python -m pytest
```

## HTTP API

- `GET /health` is a liveness check and does not require Nebius configuration.
- `GET /ready` returns 200 only when an API key and model ID are configured.
  This is a configuration check, not a remote Nebius availability probe.
- `POST /ai/interpret` accepts:

  ```json
  {
    "message": "I need a plumber in Riverbend this Saturday",
    "conversation_context": "Optional short, non-sensitive context"
  }
  ```

  The service extracts only text explicitly present in the message/context.
  A community reference is a user-provided name or phrase, never a community
  ID. Missing fields remain null and essential omissions produce a
  clarification result. The endpoint does not authenticate users in this
  initial phase; do not include user IDs, roles, credentials, or private
  account data in the request.

The request and conversation context are bounded in size. Request bodies,
prompts, model responses, and API keys are not logged by this service. Model
timeouts return 504; upstream failures and malformed model output return 502;
missing configuration returns 503. The endpoint never performs state-changing
operations.

## Next-phase integration

When authenticated marketplace context is added, the frontend or a trusted
server-side orchestrator must authenticate with Spring Boot and pass only the
minimum relevant context through an approved contract. Do not forward bearer
tokens, user IDs, roles, or private account data to the model. Spring Boot must
derive identity from its own verified token, authorize community membership,
and independently validate all data and actions. A future AI tool call may
request a backend operation, but only Spring Boot can confirm or execute it.
