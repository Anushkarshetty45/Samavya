from pydantic import Field, SecretStr, field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    nebius_api_key: SecretStr | None = None
    nebius_model: str = "nvidia/NVIDIA-Nemotron-3-Nano-30B-A3B"
    nebius_base_url: str = "https://api.tokenfactory.nebius.com/v1/"
    nebius_timeout_seconds: float = Field(default=20.0, gt=0, le=120)

    @field_validator("nebius_base_url")
    @classmethod
    def require_https(cls, value: str) -> str:
        if not value.startswith("https://"):
            raise ValueError("Nebius base URL must use HTTPS")
        return value.rstrip("/") + "/"

    @property
    def is_configured(self) -> bool:
        return bool(
            self.nebius_api_key
            and self.nebius_api_key.get_secret_value().strip()
            and self.nebius_model.strip()
        )
