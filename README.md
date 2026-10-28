# sactions-service — Screening & Customer Risk Assessment

Backend service for **Intact ID** that handles two KYC/AML jobs:

1. **Screening** — checks a person or business against sanctions, debarment and
   PEP (politically exposed person) lists via the [OpenSanctions](https://www.opensanctions.org/) API.
2. **Customer Risk Assessment (CRA)** — scores a customer as `LOW`, `MEDIUM` or
   `HIGH` risk using a **configurable, versioned risk model** that each tenant
   defines as data. Screening results feed straight into the score.

The service is multi-tenant: every tenant owns its own risk models and assessments.

---

## Contents

- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [How scoring works](#how-scoring-works)
- [API reference](#api-reference)
- [Risk model config format](#risk-model-config-format)
- [Database](#database)
- [Deployment](#deployment)
- [Project docs](#project-docs)
- [Known gaps](#known-gaps)

---

## Tech stack

| Area | Choice |
|---|---|
| Language / runtime | Java 25 |
| Framework | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Actuator) |
| HTTP client | Spring Cloud OpenFeign (OpenSanctions) |
| Database | PostgreSQL 17, schema managed by Flyway |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper included) |
| Packaging | Docker multi-stage image, published by GitHub Actions |

---

## Getting started

### Prerequisites

- JDK 25
- Docker (for the local Postgres container)
- An OpenSanctions API key

### Run locally

```bash
# 1. Start Postgres (localhost:5432, db/user/password = sactions_service)
docker compose -f compose.yaml up -d

# 2. Provide your OpenSanctions key
export OPENSANCTIONS_API_KEY=your-key-here

# 3. Run the app (Flyway migrates the schema on startup)
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080` with no context path.

> `spring-boot-docker-compose` is on the classpath, so `./mvnw spring-boot:run`
> will also start `compose.yaml` automatically if Docker is running.

### Useful URLs

| URL | What |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive API docs |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Health check |

### Build & test

```bash
./mvnw clean package          # build the jar into target/
./mvnw test                   # run tests
docker build -t sactions-service .
```

---

## Configuration

All settings live in `src/main/resources/application.yaml` and can be overridden
with environment variables.

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/sactions_service` | JDBC URL |
| `DB_USERNAME` | `sactions_service` | DB user |
| `DB_PASSWORD` | `sactions_service` | DB password |
| `OPENSANCTIONS_API_KEY` | *(dev key in yaml — see Known gaps)* | OpenSanctions API key |
| `SERVER_PORT` | `8080` | HTTP port |

OpenSanctions matching options (`application.yaml → open-sanctions`):

| Key | Value | Meaning |
|---|---|---|
| `algorithm` | `logic-v2` | OpenSanctions matching algorithm |
| `threshold` | `0.8` | Minimum score for a result to count as a match |
| `topics` | `sanction, sanction.linked, debarment, role.pep` | Lists searched |

---

## Architecture

```
com.codify.sactionsservice
├── screening/        OpenSanctions integration (complete, standalone)
│   ├── api/          MatchController  →  /api/v1/match/*
│   ├── client/       Feign client + request DTOs
│   └── service/      Person/Business match services, ScreeningResultMapper
├── risk/             CRA engine
│   ├── api/          AssessmentController, RiskModelController
│   ├── config/       Config DTOs, validator, mappers
│   ├── domain/       JPA entities + enums
│   ├── engine/       Pure scoring pipeline (no Spring, no JPA)
│   ├── repository/   Spring Data repositories
│   └── service/      AssessmentService, RiskModelService, ScreeningAttributeMapper
├── tenant/           Tenant CRUD
└── common/           ApiResult envelope, exceptions, JSONB converters, config
```

Design rules the code follows:

- **Everything a tenant can tune is data, not code** — weights, rules,
  thresholds and overrides are database rows, never Java constants.
- **Nothing is mutated once active** — risk model versions and assessments are
  append-only. Changing a model means a new draft version.
- **`risk.engine` is framework-free** — plain records in, plain records out, so
  it's unit-testable without a Spring context.
- **`risk` depends on `screening`, never the reverse.**

---

## How scoring works

`POST /v1/assessments` runs this flow:

```
 request ──► load tenant's ACTIVE risk model (by customerType)
         ──► screen customer via OpenSanctions
         ──► enrich payload: sanctions = verdict, pep = bool, adverse_media = bool
         ──► ScoringPipeline
               1. AttributeScorer   value → first matching rule (by priority) → 0-100
               2. CategoryRollup    attributes → category score → category level
               3. OverallCombiner   categories → overall score → overall level
               4. Override rules    may RAISE the level (never lower it)
         ──► persist RiskAssessment + CategoryResults + ContributionLines
```

**Screening verdicts**

| Verdict | When |
|---|---|
| `NO_HIT` | No results above the threshold |
| `POTENTIAL_MATCH` | Matches found, none scored 1.0 |
| `CONFIRMED_MATCH` | At least one match scored 1.0 |

**Attribute rules** (`matchType`): `EXACT`, `BOOLEAN`, `SET`, `RANGE`, `DEFAULT`.
Rules are tried in ascending `priority`; the first match wins.

**Missing values** (`missingBehavior`):

| Value | Effect |
|---|---|
| `DEFAULT_SCORE` | Use the attribute's `defaultScore` (optional attributes only) |
| `MAX_RISK` | Score 100 |
| `INCOMPLETE` | Score 100 and flag the whole assessment as incomplete |

**Category rollup** (`rollupMethod`): `WEIGHTED_SUM`, `WEIGHTED_AVG`, `AVERAGE`, `MAX`.
**Overall combine** (`combineMethod`): `WEIGHTED_SUM`, `MAX`, `HYBRID` (currently behaves like `WEIGHTED_SUM`).

**Stages**: `ONBOARDING` scores only categories with `timing = ONBOARDING`;
`POST_ONBOARDING` scores everything.

Every scored attribute is stored as a **contribution line** (raw value, matched
rule, points, weight, weighted points, note), so each assessment can explain
exactly how it reached its level.

---

## API reference

Risk and tenant endpoints wrap responses in a standard envelope:

```json
{
  "data": { },
  "status": "SUCCESS | FAILED",
  "message": "human-readable summary",
  "isTechnical": false,
  "errors": [{ "field": "...", "message": "..." }]
}
```

Status codes: `200` read OK · `201` created · `400` validation (see `errors`) ·
`404` not found · `409` conflict (e.g. activating a non-draft model).

### Tenants

| Method | Path | Description |
|---|---|---|
| `POST` | `/v1/tenant` | Create a tenant |
| `GET` | `/v1/tenant?id={id}` | Get a tenant |
| `PATCH` | `/v1/tenant?id={id}` | Update a tenant |

```json
POST /v1/tenant
{
  "tenantIdentifier": "acme-bank",
  "tenantType": "ENTITY",
  "tenantName": "Acme Bank",
  "description": "Retail bank client",
  "extraDetails": {}
}
```

`tenantType`: `SERVICE` (internal service) or `ENTITY` (external client).

### Risk models

| Method | Path | Description |
|---|---|---|
| `POST` | `/v1/risk-models?tenantId={id}` | Create a DRAFT model (version auto-assigned) |
| `POST` | `/v1/risk-models/{id}/activate` | Activate a draft; retires the current ACTIVE one |

Lifecycle: `DRAFT → ACTIVE → RETIRED`. Only one ACTIVE model per tenant and customer type.

### Assessments

| Method | Path | Description |
|---|---|---|
| `POST` | `/v1/assessments` | Screen + score a customer |
| `GET` | `/v1/assessments/{id}?include=breakdown` | Fetch an assessment (with contribution lines) |

```json
POST /v1/assessments
{
  "tenantId": "…",
  "customerId": "cust-123",
  "customerType": "INDIVIDUAL",
  "stage": "POST_ONBOARDING",
  "triggeredBy": "ONBOARDING",
  "attributePayload": {
    "age": 34,
    "country_of_residence": "KE",
    "product_tier": "PREMIUM"
  },
  "personIdentity": {
    "firstName": "Jane",
    "lastName": "Doe",
    "fatherName": null,
    "birthDate": "1991-04-12"
  }
}
```

- `customerType`: `INDIVIDUAL` (requires `personIdentity`) or `BUSINESS`
  (requires `businessIdentity: { "name", "jurisdiction" }`).
- `triggeredBy`: `ONBOARDING`, `SCREENING_EVENT`, `PERIODIC`, `MANUAL`.
- `sanctions`, `pep` and `adverse_media` are filled in from screening — don't send them.

### Raw screening

| Method | Path | Body |
|---|---|---|
| `POST` | `/api/v1/match/person` | `{ "firstName", "lastName", "fatherName", "birthDate" }` |
| `POST` | `/api/v1/match/business` | `{ "name", "jurisdiction" }` |

Returns `{ "verdict": "...", "matches": [{ id, caption, score, datasets, topics, sourceUrls }] }`
directly (no envelope).

---

## Risk model config format

The body of `POST /v1/risk-models`. Versions are assigned by the server.

```json
{
  "customerType": "INDIVIDUAL",
  "combineMethod": "WEIGHTED_SUM",
  "description": "Retail individuals v1",
  "overallThresholds": [
    { "riskLevel": "LOW",    "minScore": 0,  "maxScore": 39.99 },
    { "riskLevel": "MEDIUM", "minScore": 40, "maxScore": 69.99 },
    { "riskLevel": "HIGH",   "minScore": 70, "maxScore": 100 }
  ],
  "overrideRules": [
    {
      "condition": { "attribute": "sanctions", "op": "eq", "value": "CONFIRMED_MATCH" },
      "forceLevel": "HIGH",
      "reason": "Confirmed sanctions match"
    }
  ],
  "categories": [
    {
      "key": "geography",
      "displayName": "Geography",
      "weight": 0.6,
      "rollupMethod": "WEIGHTED_AVG",
      "timing": "ONBOARDING",
      "ordering": 1,
      "thresholds": [
        { "riskLevel": "LOW",    "minScore": 0,  "maxScore": 39.99 },
        { "riskLevel": "MEDIUM", "minScore": 40, "maxScore": 69.99 },
        { "riskLevel": "HIGH",   "minScore": 70, "maxScore": 100 }
      ],
      "attributes": [
        {
          "attributeDefinitionKey": "country_of_residence",
          "weight": 1,
          "required": true,
          "missingBehavior": "MAX_RISK",
          "rules": [
            { "matchType": "SET",     "matchValue": { "in": ["KE", "UG", "TZ"] }, "score": 20, "priority": 1 },
            { "matchType": "DEFAULT", "matchValue": {},                          "score": 60, "priority": 99 }
          ]
        }
      ]
    }
  ]
}
```

**Rule `matchValue` shapes**: `EXACT`/`BOOLEAN` → `{ "eq": … }` · `SET` → `{ "in": [ … ] }` ·
`RANGE` → `{ "min": …, "max": … }` · `DEFAULT` → `{}`.

**Override conditions** are a small JSON grammar:

- Leaf: `{ "attribute": "pep", "op": "eq", "value": true }`
- Composite: `{ "all": [ … ] }` or `{ "any": [ … ] }`
- Operators: `eq`, `ne`, `in`, `not_in`, `gt`, `gte`, `lt`, `lte`, `exists`

`forceLevel` is `MEDIUM` or `HIGH`; overrides can only raise a level.

**Validation** (on create/activate) checks, among other things, that scores are
within 0–100, threshold bands are contiguous and cover 0–100, keys are unique,
and category weights are present. A model without a sanctions override gets a
warning on draft and is blocked on activation.

**Seeded attribute definitions** (platform catalog, see `V15`/`V16` migrations):
`age`, `country_of_residence`, `product_tier`, `pep`, `adverse_media`, plus the
reserved `sanctions` key.

---

## Database

Flyway migrations in `src/main/resources/db/migration` (`V1`–`V16`). Hibernate
runs with `ddl-auto: validate`, so the schema must always come from a migration.

Main tables: `tenant`, `attribute_definition`, `risk_model`, `category`,
`attribute`, `value_score_rule`, `threshold`, `override_rule`,
`risk_assessment`, `category_result`, `contribution_line`.

Full entity reference: [`docs/cra-data-model.md`](docs/cra-data-model.md).

---

## Deployment

- **CI** — `.github/workflows/deploy.yml` builds the Docker image on every push
  to `main` and pushes `sactions-service:latest` and `sactions-service:<sha>` to
  Docker Hub (`DOCKERHUB_USERNAME` / `DOCKERHUB_TOKEN` secrets).
- **Server** — `docker-compose.yml` runs the image with an `.env` file on the
  external `app-net` network. It's labelled for **Watchtower**, which pulls new
  `latest` images automatically.

Required `.env` on the server: `DOCKERHUB_USERNAME`, `SERVER_PORT`,
`SACTIONS_SERVICE_PORT`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`,
`OPENSANCTIONS_API_KEY`.

---

## Project docs

| Doc | Contents |
|---|---|
| [`docs/cra-data-model.md`](docs/cra-data-model.md) | Full entity reference and ERD |
| [`docs/cra-engine-implementation-plan.md`](docs/cra-engine-implementation-plan.md) | Phased build plan and design constraints |
| [`docs/ui-handover.md`](docs/ui-handover.md) | Frontend integration guide |

---

## Known gaps

- **No authentication** — all endpoints are open. Don't expose the service publicly yet.
- **API key default** — `application.yaml` contains a fallback OpenSanctions key;
  it should be removed and `OPENSANCTIONS_API_KEY` made mandatory.
- **Adverse media is approximated** from screening topics (sanction, debarment,
  crime, etc.), so it can overlap with the sanctions signal.
- **Engine errors** — a value with no matching rule and no `DEFAULT` rule, or a
  score outside every band, raises an exception (HTTP 500).
- **Tests** — only the context-load test exists; the scoring engine needs unit tests.
- Screening endpoints use `/api/v1/...` and no envelope, unlike the rest of the API.
