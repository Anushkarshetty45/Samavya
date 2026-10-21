from decimal import Decimal
from enum import StrEnum

from pydantic import BaseModel, ConfigDict, Field, field_validator


class IntentType(StrEnum):
    SEARCH_SERVICES = "SEARCH_SERVICES"
    CREATE_REQUIREMENT = "CREATE_REQUIREMENT"
    GENERAL_QUESTION = "GENERAL_QUESTION"
    NEEDS_CLARIFICATION = "NEEDS_CLARIFICATION"


class ActionIntent(StrEnum):
    SEARCH_SERVICES = "SEARCH_SERVICES"
    CREATE_REQUIREMENT = "CREATE_REQUIREMENT"


class RequirementType(StrEnum):
    SERVICE = "SERVICE"
    PRODUCT = "PRODUCT"
    RENTAL = "RENTAL"


class IntentEntities(BaseModel):
    model_config = ConfigDict(extra="forbid")

    service_category: str | None = Field(default=None, max_length=120)
    requirement_type: RequirementType | None = None
    requirement_title: str | None = Field(default=None, max_length=200)
    quantity: Decimal | None = Field(
        default=None,
        gt=0,
        max_digits=12,
        decimal_places=2,
    )
    quantity_unit: str | None = Field(default=None, max_length=40)
    budget: Decimal | None = Field(
        default=None,
        ge=0,
        max_digits=12,
        decimal_places=2,
    )
    community_reference: str | None = Field(default=None, max_length=160)
    requested_datetime: str | None = Field(default=None, max_length=120)

    @field_validator(
        "service_category",
        "requirement_title",
        "quantity_unit",
        "community_reference",
        "requested_datetime",
        mode="before",
    )
    @classmethod
    def normalize_optional_text(cls, value: object) -> object:
        if isinstance(value, str):
            normalized = value.strip()
            return normalized or None
        return value


class ModelIntent(BaseModel):
    model_config = ConfigDict(extra="forbid")

    intent: IntentType
    intended_action: ActionIntent | None = None
    entities: IntentEntities


class InterpretRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    message: str = Field(min_length=1, max_length=4000)
    conversation_context: str | None = Field(default=None, max_length=2000)

    @field_validator("message")
    @classmethod
    def reject_blank_message(cls, value: str) -> str:
        normalized = value.strip()
        if not normalized:
            raise ValueError("message must not be blank")
        return normalized

    @field_validator("conversation_context")
    @classmethod
    def normalize_context(cls, value: str | None) -> str | None:
        if value is None:
            return None
        return value.strip() or None


class InterpretResponse(BaseModel):
    intent: IntentType
    target_intent: ActionIntent | None
    entities: IntentEntities
    missing_information: list[str]
    clarification_question: str | None
    response: str | None
