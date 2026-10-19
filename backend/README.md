# Backend database setup

The backend uses Spring JDBC with PostgreSQL and runs the existing Flyway
migrations from `infra/migrations/` on startup. They are packaged at
`db/migration` without moving or editing the versioned SQL files.

## Local development

Requires Java 21 and Docker Compose. From the repository root, create an
ignored `.env` file once for the local database volume, then start Compose and
the backend:

```powershell
if (-not (Test-Path .env)) {
  "DB_PASSWORD=$([guid]::NewGuid().ToString('N'))" | Set-Content .env
}
$env:DB_PASSWORD = (Get-Content .env |
  Where-Object { $_ -match '^DB_PASSWORD=' } |
  Select-Object -First 1) -replace '^DB_PASSWORD=', ''
docker compose --env-file .env -f infra\compose.yaml up -d
Set-Location backend
.\mvnw.cmd spring-boot:run
```

Keep this local password for as long as the Compose database volume exists;
changing the environment variable does not change a password already stored in
PostgreSQL.

The Compose service listens on `localhost:5433`; the backend defaults to
`jdbc:postgresql://localhost:5433/samavya` and username `samavya_user`.
Configuration can be overridden with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`. Do not commit credentials or reuse production credentials for
local development.

Flyway applies V1 and then V2. Before pointing the backend at an existing
database, inspect its schema and `flyway_schema_history` first. Do not baseline,
reset, or manually run these migrations against an existing schema without
reviewing its current state.

## Tests

`.\mvnw.cmd test` runs the category API integration tests against a disposable
PostGIS Testcontainers database when a Docker engine supported by Testcontainers
is available. Those tests do not connect to or migrate the local Compose
database.
