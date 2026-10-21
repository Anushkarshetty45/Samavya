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
`DB_PASSWORD`. Authentication also requires `JWT_SECRET`, which must be
Base64-encoded cryptographically random bytes of at least 32 bytes. Generate a
local value in PowerShell with:

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

Set the variables in the backend process environment or an ignored local
environment file; do not commit credentials or reuse production credentials
for local development. JWTs are signed with HS256 and expire after 15 minutes.
Passwords are hashed with BCrypt and are never included in API responses.

Flyway applies V1 and then V2. Before pointing the backend at an existing
database, inspect its schema and `flyway_schema_history` first. Do not baseline,
reset, or manually run these migrations against an existing schema without
reviewing its current state.

## Marketplace read APIs

- `GET /api/provider-profiles/{providerId}` returns the provider's public
  profile fields: `id`, `providerName`, `description`, `available`, and
  `verificationStatus`. Missing profiles return 404; non-positive IDs return
  400. It does not expose internal user IDs, email, or phone.
- `GET /api/service-discovery` lists actual active provider-service offerings,
  including provider/category details, `priceFrom`, and active gigs. A category
  by itself is not treated as an offering. Each active gig includes its
  `pricingType`, nullable `price`, and status.
- Optional `communityId` and `categoryId` query parameters filter discovery,
  and can be combined. Non-positive filter values return 400.

These routes are read-only. Profile, offering, and gig create/update endpoints
are intentionally unavailable until provider ownership workflows are designed
and implemented. The existing demo service endpoints are unchanged.

## Authentication and community APIs

The backend issues stateless bearer JWTs; registration and login are public.
All other routes require a valid token except the legacy demo GET endpoints and
service-category GET endpoints, which remain public.

- `POST /api/auth/register` accepts `{ "name", "email", "password" }`, creates
  an ACTIVE account, and returns only `id`, `name`, and `email`. Passwords must
  be at least 12 characters and no more than 72 UTF-8 bytes.
- `POST /api/auth/login` accepts `{ "email", "password" }` and returns
  `{ "accessToken", "tokenType", "expiresIn", "user" }`.
- `GET /api/auth/me` returns the authenticated account, derived from the
  verified token rather than a client-supplied user ID.
- `GET /api/communities` and `GET /api/communities/{id}` list and retrieve
  communities for authenticated users so they can discover communities to join.
- `POST /api/communities` accepts `{ "name", "description", "address" }`.
  The authenticated creator is added as a MEMBER atomically with creation.
- `POST /api/communities/{id}/memberships` joins the caller as MEMBER. Repeated
  joins reactivate the existing membership instead of creating duplicates.
- `GET /api/communities/me/memberships` returns only the authenticated user's
  memberships; the endpoint does not accept a target user ID or role.

Community-scoped marketplace discovery requires an ACTIVE membership. An
unfiltered discovery request returns offerings only from communities where the
authenticated user has an ACTIVE membership. There is no endpoint to self-assign
PROVIDER or administrative roles. Authentication and authorization failures
return the shared JSON error shape with 401 or 403 status codes.

## Provider profiles, offerings, and gigs

All provider-management endpoints require a bearer token. The user ID is
derived from the token; none of the write DTOs accept provider or user IDs.
Profiles are created with `PENDING` verification status and clients cannot
change verification status. Offering writes and gig writes require active
membership in the offering's community. Provider-owned resources belonging to
another account are returned as 404.

- `POST /api/provider-profile/me` creates the caller's profile, or returns the
  existing profile if one already exists. Accepts optional `description` and
  `available`; the latter defaults to true on first creation.
- `GET /api/provider-profile/me` retrieves the caller's profile.
- `PUT /api/provider-profile/me` updates `description` and required `available`;
  verification status is read-only.
- `POST /api/provider-services` accepts `communityId`, `categoryId`, and
  nullable `priceFrom`. The caller must have a provider profile, active
  community membership, and the service category must be active.
- `GET /api/provider-services/me` lists the caller's offerings.
- `PUT /api/provider-services/{offeringId}` replaces `priceFrom` and required
  `active`; setting `active` false deactivates the offering. The existing
  provider/community/category unique constraint is preserved and duplicates
  return 409.
- `POST /api/provider-services/{offeringId}/gigs` creates a gig with `title`,
  optional `description`, `pricingType`, and `price`. FIXED and HOURLY require
  a nonnegative price; QUOTE requires a null price.
- `GET /api/gigs/me` lists the caller's gigs; `GET /api/gigs/{gigId}` retrieves
  one owned gig; `PUT /api/gigs/{gigId}` updates its title, description, pricing
  type, and price while it is not closed.
- `POST /api/gigs/{gigId}/pause` transitions ACTIVE to PAUSED;
  `/resume` transitions PAUSED to ACTIVE; `/close` transitions ACTIVE or PAUSED
  to terminal CLOSED. Invalid transitions return 409.

## Community requirements

Requirements are stored in the existing `requirements` table; no schema
migration is needed. Every requirements route requires a bearer token and
ACTIVE community membership. A service requirement must reference an active
service category; product and rental requirements must not include a service
category. Optional quantity and budget values follow the database's positive
quantity and nonnegative budget checks.

- `POST /api/communities/{communityId}/requirements` accepts `type`,
  `serviceCategoryId` (SERVICE only), `title`, optional `description`,
  `quantity`, `quantityUnit`, `budget`, and `neededBy`. The authenticated user
  becomes `createdBy`; any client-supplied creator field is ignored.
- `GET /api/communities/{communityId}/requirements?type=SERVICE|PRODUCT|RENTAL`
  lists the community's requirements, optionally filtered by type.
- `GET /api/requirements/{requirementId}` retrieves a requirement only for an
  active member of its community.
- `POST /api/requirements/{requirementId}/cancel` cancels an OPEN requirement
  only when called by its creator. Missing records return 404, membership or
  ownership failures return 403, invalid input returns 400, and invalid state
  transitions return 409.

## Tests

`.\mvnw.cmd test` runs unit and controller tests, plus category and marketplace
and authentication/community/requirements integration tests against disposable
PostGIS Testcontainers databases when a Docker engine supported by
Testcontainers is available. Those tests do not connect to or migrate the
local Compose database.
