Samavya

Your community. One conversation. Endless possibilities.

Samavya is an AI powered, conversational community marketplace that connects people with local service providers, shared purchasing opportunities, and rental services. Users simply describe what they need, and Samavya helps discover relevant listings, coordinate requests, manage bookings, and report issues, all through one assistant.

 Demo

- Live application:
- Demo video: 
- Presentation:

 1)Features

- Conversational AI: Describe needs naturally through chat, with voice input where supported.
- Community based discovery: Find relevant local services and existing requests.
- Smart coordination: Identify similar needs and opportunities for shared visits or bulk orders.
- Service bookings: Discover providers, check availability, and manage fulfilment.
- Rentals and local commerce:  Support temporary rentals and community purchasing.
- Complaint management: Report service issues conversationally and track their resolution.
- Provider reliability: Use fulfilment history and resolved complaints to support fair recommendations.

 2)How it works

1. A user describes a need through the conversational interface.
2. The AI extracts structured details and requests relevant information from Samavya's backend.
3. The backend retrieves real listings, checks permissions and availability, and returns suitable options.
4. The user confirms the desired action.
5. Providers and customers track fulfilment, completion, and any disputes.

 3)Tech stack

| Component      | Technologies                             |
| -------------- | ---------------------------------------- |
| Frontend       | Next.js, React, TypeScript, Tailwind CSS |
| Backend        | Java 21, Spring Boot, Spring Security    |
| AI service     | Python, FastAPI                          |
| AI inference   | Nebius Token Factory, NVIDIA Nemotron    |
| Database       | PostgreSQL, PostGIS                      |
| Infrastructure | Docker, Docker Compose                   |
| Testing        | JUnit, pytest, Playwright                |

4)Architecture

flowchart 
    A[User] <--> B[Next.js Frontend]
    B <--> C[Spring Boot API]
    C <--> D[(PostgreSQL + PostGIS)]
    C <--> E[FastAPI AI Service]
    E <--> F[Nebius Token Factory]
    F --- G[NVIDIA Nemotron]


The separate FastAPI service currently extracts intent using Nebius Token Factory
and NVIDIA Nemotron. It does not call the backend or access marketplace data.
The Spring Boot backend remains authoritative for authentication, authorization,
marketplace data, and any future state changes.

5)Getting started

5.1)Prerequisites

- Git
- Docker Desktop with Docker Compose for the local PostgreSQL/PostGIS database
- Java 21 for the Spring Boot backend
- Python 3.11 or newer for the AI service
- A Nebius Token Factory API key with access to a listed NVIDIA Nemotron model

6)Installation

git clone <REPOSITORY_URL>
cd samavya

7)Configure and start the backend/database by following
[`backend/README.md`](backend/README.md). The Compose file starts PostgreSQL;
the backend is started separately.

8)Configure and run the independent AI service using
[`ai-service/README.md`](ai-service/README.md). Its tests use mocked model
responses and do not require a Nebius API key. The frontend is not yet connected
to this service.

9)Configuration

Backend and AI-service environment variables are configured separately. See
[`backend/README.md`](backend/README.md) and
[`ai-service/README.md`](ai-service/README.md); do not put secrets in frontend
configuration or commit local `.env` files.

10)Testing

For AI-service tests, run `python -m pytest` from `ai-service/`. For backend
build and test commands, see [`backend/README.md`](backend/README.md).

 Repository structure

samavya/
├── frontend/           Conversational UI
├── backend/            Core APIs and business logic
├── ai-service/         AI orchestration and inference
├── infra/              Compose configuration and SQL migrations
└── README.md


11)Security and responsible AI

- Backend enforced authentication and authorization.
- Server side validation of AI proposed actions.
- User confirmation for consequential operations.
- No direct database access or arbitrary execution by the language model.
- Complaint and provider reliability workflows designed to account for disputes and verified outcomes.
- Secrets managed outside source control.

Built with

Samavya integrates [Nebius Token Factory](https://tokenfactory.nebius.com/) and NVIDIA Nemotron to power its conversational AI experience.
**Samavya — bringing community needs and local services together, one conversation at a time.**

helooooooo 