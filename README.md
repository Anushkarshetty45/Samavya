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


The AI interprets user intent and helps plan actions. The Spring Boot backend validates permissions, availability, booking state, and confirmations before changing application data.

5)Getting started

5.1)Prerequisites

- Git
- Docker Desktop with Docker Compose
- A Nebius Token Factory API key with access to the configured NVIDIA Nemotron model

6)Installation

git clone <REPOSITORY_URL>
cd samavya


7)Configure the environment:

cp .env.example .env


Add the required database settings and AI API credentials to `.env`. 

8)Start the application:


docker compose up --build
Open the application at the local URL specified in the deployment configuration.
See [`docs/SETUP.md`](docs/SETUP.md) for detailed configuration, database initialization, troubleshooting, and any required model settings.

9)Environment variables

See [`.env.example`](.env.example) for the complete list of supported configuration variables. These may include database connection settings, the Nebius API key, the model identifier, and internal service URLs.

10)Testing

Run the relevant test suites using the commands documented in [`docs/SETUP.md`](docs/SETUP.md).

The project includes tests for backend business logic, AI service behavior, and end to end user flows.

 Repository structure

samavya/
├── frontend/           Conversational UI
├── backend/            Core APIs and business logic
├── ai-service/         AI orchestration and inference
├── database/           Migrations and seed data
├── docs/               Setup and technical documentation
├── docker-compose.yml
├── .env.example
├── LICENSE
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