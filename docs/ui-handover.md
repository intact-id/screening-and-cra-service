# sactions-service — UI/Frontend Integration Guide

Backend for sanctions/PEP/adverse-media screening and the Customer Risk
Assessment (CRA) engine. This doc covers what's actually built and callable
today — not the full long-term API surface (see `cra-engine-implementation-plan.md`
for what's still ahead).

## Running locally

```
docker compose up -d      # starts Postgres on localhost:5432
mvn spring-boot:run        # starts the app on localhost:8080, no context path
```

No auth layer exists yet — every endpoint below is unauthenticated. Don't
build login/token handling against this service yet; there's nothing to
integrate with.

## Interactive API docs (Swagger)

Once the app is running:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Raw OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Use Swagger UI to explore/try requests directly — this doc is the narrative
version of the same surface.

## Response envelope

Every `risk`/`tenant` endpoint (not `screening`'s `/api/v1/match/*`, see below)
returns the same wrapper, regardless of success or failure:

```json
{
  "data": { ... } | null,
  "status": "SUCCESS" | "FAILED",
  "message": "human-readable summary",
  "isTechnical": false,
  "errors": [ { "field": "...", "message": "..." } ] | null
}
```

- `data` — the actual payload on success, `null` on failure.
- `errors` — only populated for field-level validation failures (`400`). Use
  this to show inline form errors when creating/activating a risk model or
  running an assessment. `null` for every other outcome.
- `isTechnical` — `true` means "something broke server-side, not user error"
  (e.g. an unexpected exception) — a reasonable signal for whether to show a
  generic "something went wrong" toast vs. surfacing `message` directly.

### HTTP status codes you'll see

| Status | Meaning |
|---|---|
| `200` | Read succeeded (`GET`) |
| `201` | Create succeeded (`POST`) |
| `400` | Field-level validation failure — check `errors` |
| `404` | Referenced resource doesn't exist (tenant, risk model, assessment) |
| `409` | Conflict — e.g. trying to activate a risk model that isn't in `DRAFT` |

## Endpoints

### `POST /v1/risk-models` — create a draft risk model

Query param: `tenantId` (string, required).
Body: a `RiskModelConfig` — see `cra-data-model.md` for the full shape
(categories, attributes, thresholds, override rules). This is the
compliance-authored config, not something a generic form should freely
construct — check with backend before building this UI.

Response `data`: `RiskModelResponseDto`
```json
{
  "id": "...",
  "version": 1,
  "description": "...",
  "customerType": "INDIVIDUAL" | "BUSINESS",
  "lifeCycleStatus": "DRAFT" | "ACTIVE" | "RETIRED",
  "combineMethod": "WEIGHTED_SUM" | "MAX" | "HYBRID",
  "tenantId": "...",
  "effectiveFrom": "2026-08-28T19:35:00" | null
}
```
`effectiveFrom` is `null` until the model is activated.

### `POST /v1/risk-models/{id}/activate` — activate a draft

No body. Re-validates the config (this time treating the "must have a
sanctions-override rule" check as a hard failure, not just a warning) and, if
valid, retires whatever was previously `ACTIVE` for that tenant/customerType
and flips this one to `ACTIVE`. Same `RiskModelResponseDto` shape as above,
now with `lifeCycleStatus: "ACTIVE"` and `effectiveFrom` populated.

### `POST /v1/assessments` — run a risk assessment for a customer

```json
{
  "tenantId": "...",
  "customerId": "...",
  "customerType": "INDIVIDUAL" | "BUSINESS",
  "stage": "ONBOARDING" | "POST_ONBOARDING",
  "attributePayload": { "age": 34, "country_of_residence": "KE", "...": "..." },
  "triggeredBy": "ONBOARDING" | "SCREENING_EVENT" | "PERIODIC" | "MANUAL",
  "personIdentity": { "firstName": "...", "lastName": "...", "fatherName": "...", "birthDate": "1990-01-01" },
  "businessIdentity": { "name": "...", "jurisdiction": "..." }
}
```
Only one of `personIdentity`/`businessIdentity` is required — whichever
matches `customerType`. This is used to run the sanctions/PEP screening
check, separately from `attributePayload` (which is the risk-scoring
attribute data, not search-query identity).

Response `data`: `AssessmentResponseDto`
```json
{
  "id": "...",
  "tenantId": "...",
  "customerId": "...",
  "customerType": "INDIVIDUAL",
  "riskModelId": "...",
  "modelVersion": 1,
  "stage": "ONBOARDING",
  "status": "COMPLETE" | "INCOMPLETE",
  "overallScore": 62.5,
  "overallLevel": "LOW" | "MEDIUM" | "HIGH",
  "overrideApplied": false,
  "triggeredBy": "ONBOARDING",
  "screening": {
    "verdict": "NO_HIT" | "POTENTIAL_MATCH" | "CONFIRMED_MATCH",
    "matches": [
      {
        "entityId": "...",
        "caption": "matched entity's name",
        "score": 0.87,
        "datasets": ["us_ofac_sdn", "eu_fsf", "..."],
        "topics": ["sanction", "role.pep"],
        "sourceUrls": ["https://sanctionssearch.ofac.treas.gov/...", "..."]
      }
    ]
  },
  "categoryResults": [ { "categoryKey": "...", "categoryScore": 55.0, "categoryLevel": "MEDIUM" } ],
  "contributionLines": [ ]
}
```

**Important:** this endpoint does **not** block onboarding on a confirmed
sanctions match — it always returns `201` with the full result. A confirmed
match instead shows up as `overallLevel: "HIGH"` (the model's mandatory
override rule forces this) plus the actual match evidence in `screening` —
the UI is expected to surface that and let the compliance workflow decide
what happens next, not treat a `CONFIRMED_MATCH` verdict as an error state.

`contributionLines` is always populated by the backend currently (the
`includeBreakdown` toggle only applies to the `GET` endpoint below).

### `GET /v1/assessments/{id}?include=breakdown` — fetch a past assessment

Same `AssessmentResponseDto` shape as above. Omit `?include=breakdown` (or
pass any other value) to get `categoryResults` without `contributionLines`
(empty array) — useful for a summary view vs. a full audit-trail view.

### `POST /api/v1/match/person` / `POST /api/v1/match/business` — raw screening lookup

These are **not** wrapped in the `ApiResult` envelope — they return the
`ScreeningResult` shape directly (the `screening` object shown above), with
no `data`/`status` wrapper. Body is a `PersonMatchDto`
(`firstName`/`lastName`/`fatherName`/`birthDate`) or `BusinessMatchDto`
(`name`/`jurisdiction`). Useful for a standalone "screen this name" tool
separate from a full risk assessment.

## Known gaps — don't build UI assuming these exist yet

- **No auth.** Every `tenantId`/`customerId` is trusted as-is from the request.
- **Currency normalization isn't implemented.** `rawValue`/`normalizedValue`
  in `contributionLines` are currently identical — don't build a "converted
  amount" display expecting a real conversion yet.
- **EDD queue / MLRO escalation are log lines only** — there's no queue or
  workflow system to surface a "flagged for review" list from yet.
- **`pep`/`adverse_media` in `attributePayload` are ignored if submitted** —
  the backend always overwrites them from the real `screening` result
  (`role.pep` topic for `pep`; `sanction`/`debarment`/`poi`/`wanted`/`crime.*`
  for `adverse_media`), so don't build a UI that lets a caller set these
  directly.
