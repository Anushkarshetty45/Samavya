from contextlib import asynccontextmanager
from typing import AsyncIterator

from fastapi import FastAPI, HTTPException

from samavya_ai.config import Settings
from samavya_ai.nebius import (
    ModelResponseError,
    ModelTimeoutError,
    ModelUnavailableError,
    NebiusIntentInterpreter,
)
from samavya_ai.schemas import InterpretRequest, InterpretResponse


def create_app(
    settings: Settings | None = None,
    interpreter: NebiusIntentInterpreter | None = None,
) -> FastAPI:
    service_settings = settings or Settings()
    intent_interpreter = interpreter or NebiusIntentInterpreter(service_settings)

    @asynccontextmanager
    async def lifespan(_: FastAPI) -> AsyncIterator[None]:
        yield
        await intent_interpreter.close()

    app = FastAPI(
        title="Samavya AI Service",
        version="0.1.0",
        lifespan=lifespan,
    )

    @app.get("/health")
    async def health() -> dict[str, str]:
        return {"status": "ok"}

    @app.get("/ready")
    async def readiness() -> dict[str, str]:
        if not service_settings.is_configured:
            raise HTTPException(
                status_code=503,
                detail={
                    "code": "nebius_not_configured",
                    "message": "Nebius API key and model must be configured",
                },
            )
        return {"status": "ready"}

    @app.post("/ai/interpret", response_model=InterpretResponse)
    async def interpret(request: InterpretRequest) -> InterpretResponse:
        if not service_settings.is_configured:
            raise HTTPException(
                status_code=503,
                detail={
                    "code": "nebius_not_configured",
                    "message": "Nebius API key and model must be configured",
                },
            )
        try:
            return await intent_interpreter.interpret(request)
        except ModelTimeoutError:
            raise HTTPException(
                status_code=504,
                detail={
                    "code": "nebius_timeout",
                    "message": "The intent service timed out",
                },
            ) from None
        except ModelResponseError:
            raise HTTPException(
                status_code=502,
                detail={
                    "code": "invalid_model_response",
                    "message": "The model returned an invalid response",
                },
            ) from None
        except ModelUnavailableError:
            raise HTTPException(
                status_code=502,
                detail={
                    "code": "nebius_upstream_error",
                    "message": "The intent service is temporarily unavailable",
                },
            ) from None

    return app


app = create_app()
