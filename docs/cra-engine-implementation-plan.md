# Customer Risk Assessment (CRA) Engine — Implementation Plan

Status: planning
Owner: sactions-service
Depends on: `screening` module (OpenSanctions integration, complete)
Target module: `risk` (not yet started)

This plan sequences the build described in the CRA design & spec doc into shippable
phases. Each phase has a goal, concrete deliverables, and an exit criterion — the
condition that must hold before moving to the next phase. Phases are ordered by
dependency, not by priority; §5 (screening integration) is regulatorily the most
important, but it can't be built before the data model and engine exist under it.

## Guiding constraints (carried from the design doc)

These aren't restated per phase — they apply throughout and should be checked in
review at every phase:

- **Everything a tenant can tune is data, not code.** Attributes,  rules, weights,
  thresholds, override conditions all live in `risk.domain` entities, never as
  Java constants.
- **Nothing is ever mutated once active.** `RiskModel` versions and
  `RiskAssessment` records are append-only. "Fixing" a score means creating a new
  assessment or a new draft version, not editing an old one.
- **`risk.engine` stays framework-free.** No Spring annotations, no JPA entities
  as direct inputs — it takes plain records/DTOs in and returns plain
  records/DTOs out. This is what makes it unit-testable without a Spring context
  and reusable for the future "simulate a new version" feature (§11 of the spec).
- **`risk` depends on `screening`, never the reverse.** The sanctions gate and
  Screening category call into `screening.service`; `screening` must never import
  from `risk`.

---

## Phase 0 — Foundations (done)

**Goal:** module boundary exists before any risk code is written.

- [x] Move all OpenSanctions integration code into `screening/` (`api`, `client`,
      `service` sub-packages).
- [x] Reserve `risk/` as the namespace for everything below.

**Exit criterion:** `screening` compiles standalone as a feature module; `risk`
package does not exist yet.

---

## Phase 1 — Data model

**Goal:** the entities in §6 of the spec exist as JPA entities with migrations,
with no behavior on top yet.

### 1.1 Dependencies
Add to `pom.xml` if not already present: `spring-boot-starter-data-jpa`,
a Postgres driver, and Flyway (`flyway-core` + `flyway-database-postgresql`).

### 1.2 Entities (`risk.domain`)

| Entity | Notes |
|---|---|
| `Tenant` | `baseCurrency` per Assumption A |
| `RiskModel` | versioned per `(tenantId, customerType)`; `status` DRAFT/ACTIVE/RETIRED; immutable once ACTIVE |
| `Category` | belongs to one `RiskModel`; `rollupMethod`, `timing` (ONBOARDING/POST_ONBOARDING) |
| `Attribute` | belongs to one `Category`; `missingBehavior`, `sourceSystem` |
| `ValueScoreRule` | belongs to one `Attribute`; `matchType`, `matchValue` (jsonb), `priority` |
| `Threshold` | polymorphic owner (`CATEGORY` \| `MODEL`) via `ownerType`/`ownerId` |
| `OverrideRule` | belongs to one `RiskModel`; `condition` (jsonb), `forceLevel` |
| `RiskAssessment` | pins `riskModelId` + denormalised `modelVersion`; `inputSnapshot`, `fxSnapshot` (jsonb) |
| `CategoryResult` | one per category per assessment |
| `ContributionLine` | one per scored attribute per assessment — the audit granularity |

### 1.3 Migrations
Flyway migration per entity (or one migration per logical group: model config
tables, then assessment/audit tables). Enforce at the DB level what can be
enforced there:
- FK constraints matching the ERD in §6.1.
- `RiskModel(tenantId, customerType, version)` unique.
- `RiskAssessment` and `ContributionLine`/`CategoryResult` have no update path in
  the repository layer (enforce in code in Phase 4, not just by convention).

### 1.4 Repositories (`risk.repository`)
Spring Data JPA repositories, one per aggregate root (`RiskModel`,
`RiskAssessment`). Child entities (`Category`, `Attribute`, `ValueScoreRule`,
`Threshold`, `OverrideRule`, `CategoryResult`, `ContributionLine`) are accessed
through their parent aggregate, not given their own top-level repositories.

**Exit criterion:** `./mvnw compile` succeeds with all entities and repos in
place; a migration can stand up an empty schema; no service/API code yet.

---

## Phase 2 — Config schema, parsing & validation

**Goal:** the JSON config document from §7 can be parsed into the Phase 1
entities and rejected if invalid, before any scoring exists.

### 2.1 Config DTOs (`risk.config.dto`)
Plain records mirroring §7.1's shape (`RiskModelConfig`, `CategoryConfig`,
`AttributeConfig`, `ValueScoreRuleConfig`, `ThresholdConfig`, `OverrideRuleConfig`).
These are the wire format; Phase 1 entities are the storage format. Keep them
separate — the config doc is versioned/immutable per model version, the entities
are how it's queried.

### 2.2 Validator (`risk.config.RiskModelConfigValidator`)
Pure function: `ValidationResult validate(RiskModelConfig)`. Rules from §4.4,
§7, and §10.4:
- Every `ValueScoreRule.score` in `[0, 100]`.
- `Threshold` bands per owner (each `Category` and the model's overall
  thresholds) are **contiguous, non-overlapping, and cover `[0, 100]`**.
- `Attribute.key` unique within a `Category`; `Category.key` unique within a
  `RiskModel`.
- Every `Category.weight` present and `> 0`; weights need not sum to 1 (normalise
  at combine time) but must be present.
- At least one `OverrideRule` targets a sanctions-confirmed-match condition
  (governance guardrail, §10.4) — **warn, don't hard-block**, since a tenant
  without sanctions screening configured yet is a valid intermediate state; make
  this a blocking check before `activate`, not before `save as draft`.
- `OverrideRule.forceLevel` can only raise — validated at evaluation time too
  (Phase 3), but reject at config time if a rule is nonsensical (e.g. targets a
  level below what a lower-priority rule already guarantees — best-effort check).

### 2.3 Persistence mapping (`risk.config.RiskModelConfigMapper`)
Maps a validated `RiskModelConfig` → the Phase 1 entity graph (new `DRAFT`
`RiskModel` + children), and the reverse (entity graph → config DTO, for the
"view current config" / diffing use case in §11).

**Exit criterion:** given a config JSON matching §7.2/§7.3, the validator
accepts it and the mapper persists a `DRAFT` `RiskModel` with full entity graph;
given a config with overlapping thresholds or duplicate keys, the validator
rejects it with a specific field-level error (not a generic failure).

---

## Phase 3 — Scoring engine (pure logic)

**Goal:** given an attribute payload and an **already-loaded** model (entities,
not IDs — no DB access inside this package), produce a full scored result. This
is the part most worth unit-testing exhaustively, since it's the regulator-facing
logic.

### 3.1 Types (`risk.engine`)
Plain input/output records — no JPA, no Spring:
- Input: `AttributePayload` (key → raw value), `RiskModelSnapshot` (the loaded
  model graph), `Stage` (ONBOARDING \| POST_ONBOARDING).
- Output: `ScoringResult` (overall score/level, override-applied flag,
  `List<CategoryScoreResult>`, `List<ContributionLineResult>`,
  `List<OverrideEvaluationResult>`).

### 3.2 `AttributeScorer`
For one attribute + its rules: find the first matching rule by `priority`
(EXACT/RANGE/SET/BOOLEAN, falling back to DEFAULT), or apply `missingBehavior`
if the value is absent — per §10.1, **required + missing → INCOMPLETE or
MAX_RISK, never a silent low DEFAULT_SCORE**; enforce this as a hard rule in
code, not just a config convention (reject a config where a required attribute
has `missingBehavior: DEFAULT_SCORE` — fold this into the Phase 2 validator too).

### 3.3 `CategoryRollup`
Given attribute scores + weights for one category, apply its `rollupMethod`
(SUM / WEIGHTED_SUM / AVERAGE / WEIGHTED_AVG / MAX) → category score, then map
through the category's `Threshold`s → category level. Only include categories
whose `timing` matches the assessment's `stage` (§5.2 — ONBOARDING categories
always included; POST_ONBOARDING only when stage is POST_ONBOARDING).

### 3.4 `OverallCombiner`
Implements the three `combineMethod`s from §4.3, but **HYBRID is the only one
new models should be allowed to select** going forward (enforce as a config-time
warning in Phase 2, not a hard block, since WEIGHTED_SUM/MAX may exist on
already-active legacy versions):
1. Weighted sum of category scores → base score → map through model-level
   `Threshold`s → base level.
2. Evaluate every `OverrideRule.condition` against the attribute payload (and,
   for the sanctions condition specifically, against the screening result passed
   in by the caller — see Phase 5). Override rules **only raise** the level;
   take the max of base level and all triggered override levels.

### 3.5 `ScoringPipeline`
Orchestrates 3.2 → 3.3 → 3.4 and assembles the full `ScoringResult` with every
`ContributionLine` populated (raw value, normalised value, matched rule id,
points, weight, weighted points) — this is what makes §9's reconstruction test
("can a compliance officer justify this months later") pass.

**Exit criterion:** unit tests covering — a plain weighted-sum case, a MAX
rollup within a category, an override that raises the level, an override that
would lower but is ignored, a missing required attribute triggering INCOMPLETE,
and the worked example from §8.3 reproduced exactly (Country MAX → 90,
30% weight → base MEDIUM, no override, matches the spec's expected output).

---

## Phase 4 — Persistence & orchestration service

**Goal:** wire Phase 1–3 together behind a service API; still no HTTP layer.

### 4.1 `RiskModelService` (`risk.service`)
- `createDraft(RiskModelConfig)` → validate (Phase 2) → persist `DRAFT`.
- `activate(riskModelId)` → retire the current `ACTIVE` version for that
  `(tenant, customerType)`, flip the new one to `ACTIVE`, stamp `effectiveFrom`.
  Enforce the sanctions-override guardrail (§10.4) as a **hard block** here,
  even if Phase 2 only warned at draft time.
- `getActive(tenantId, customerType)` → loaded `RiskModelSnapshot` for the engine.

### 4.2 `AssessmentService`
- `runAssessment(customerId, customerType, stage, attributePayload, triggeredBy)`:
  1. Load the tenant's active `RiskModelSnapshot` for `customerType`.
  2. Normalise currency attributes against `Tenant.baseCurrency`, snapshot the FX
     rate used into `fxSnapshot` (Phase 6).
  3. Run `ScoringPipeline` (Phase 3).
  4. Persist a new `RiskAssessment` + `CategoryResult`s + `ContributionLine`s.
     **Never update an existing `RiskAssessment` — always insert.**
  5. Return the result.
- `getAssessment(id, includeBreakdown)` → maps to the §8.3 response shape.

**Exit criterion:** an integration test (real DB, e.g. Testcontainers) that
creates a draft model, activates it, runs an assessment, and reads it back with
full breakdown — no HTTP involved yet.

---

## Phase 5 — API layer

**Goal:** expose Phase 4 as the REST surface in §8.

### 5.1 Endpoints (`risk.api`)
- `POST /v1/risk-models` → `RiskModelService.createDraft`.
- `POST /v1/risk-models/{id}/activate` → `RiskModelService.activate`.
- `POST /v1/assessments` → `AssessmentService.runAssessment`.
- `GET /v1/assessments/{id}?include=breakdown` → `AssessmentService.getAssessment`.

Response DTOs (`risk.api.dto`) map 1:1 to the JSON shapes in §8.1–8.3 — don't
leak JPA entities through the controller layer.

### 5.2 Validation error responses
Phase 2 validator failures should surface as `400` with a field-level error
list (`validation.warnings` / a parallel `errors` array), not a generic 500 —
this is what makes the config authoring loop usable by a compliance-minded
tenant admin, not just an engineer reading logs.

**Exit criterion:** manual/HTTP-client walkthrough of create draft → activate →
run onboarding assessment → run post-onboarding assessment → fetch breakdown,
matching the worked example in §8.3 end to end.

---

## Phase 6 — Screening integration (the compliance-critical phase)

**Goal:** implement §5's two-mechanism split correctly. This is flagged in the
spec as the highest-priority gap if missed, so treat it as its own review gate,
not a footnote on Phase 4/5.

### 6.1 Screening result mapping (`risk.screening` or inside `risk.service`)
Map the raw `screening.service` response (OpenSanctions JSON) into a small
sealed result: `NO_HIT`, `POTENTIAL_MATCH`, `CONFIRMED_MATCH` (§10.6). Treat
`score == 1.0 && match == true` (as observed in testing) as the *first-pass*
signal for `CONFIRMED_MATCH`, but this threshold should be a named constant,
reviewed against real screening-provider guidance before going live — don't
hardcode `1.0` as a magic number three places.

### 6.2 Sanctions blocking gate (onboarding, pass/fail — not a score)
Before `AssessmentService.runAssessment` completes an `ONBOARDING`-stage
assessment (or as a preceding step the caller must invoke), call into
`screening.service` synchronously. `CONFIRMED_MATCH` → block onboarding,
escalate to MLRO (at minimum: a distinct exception type the API layer turns
into a `403`-with-reason, plus whatever escalation hook exists downstream).
`POTENTIAL_MATCH` → route to EDD queue, does not block. This gate is
**independent of the Screening category and independent of `combineMethod`** —
it must run even for a tenant whose model has no Screening category configured.

### 6.3 Screening category (post-onboarding, scored)
A `POST_ONBOARDING`-stage assessment includes a `screening` `Category` whose
attributes (`pep`, `adverse_media`, etc.) are populated from the mapped
screening result, following the same `ContributionLine` path as any other
attribute — no special-casing in the engine, only in how the payload going into
`AssessmentService.runAssessment` is assembled for this stage.

**Exit criterion:** an onboarding attempt for a confirmed-match individual is
blocked before any `RiskAssessment` is persisted; a post-onboarding
re-assessment for a PEP correctly contributes to the Screening category and can
trigger the PEP override rule end to end.

---

## Phase 7 — Currency normalisation

**Goal:** Assumption A and §10.2 implemented, not deferred.

- FX source: **open question from the spec — needs a decision before this
  phase starts**, not an implementation detail to improvise. Pick one
  authoritative source per tenant and a staleness tolerance.
- `CurrencyNormalizer` (`risk.engine` or a `common` util used by it): converts a
  `CURRENCY_AMOUNT` attribute value to `Tenant.baseCurrency` using a snapshotted
  rate; the rate + as-of date is captured once per assessment into
  `RiskAssessment.fxSnapshot` and reused for every currency attribute in that
  run (not re-fetched per attribute).

**Exit criterion:** re-running Phase 3's currency-attribute test with a
mocked FX snapshot produces the same `ContributionLine.normalizedValue` on
every run, and changing "live" rates after the fact does not change a
previously persisted assessment's stored values.

---

## Phase 8 — Governance guardrails

**Goal:** §10.4's maker-checker and change-audit requirements, on top of the
validation from Phase 2.

- `RiskModel.createdBy` / `approvedBy` populated from the authenticated
  principal (requires whatever auth context the platform already has — out of
  scope of this doc if not yet decided elsewhere).
- `activate()` requires `approvedBy != createdBy` (maker-checker) unless a
  tenant is explicitly configured to skip this (e.g. a solo-founder tenant in
  sandbox mode) — make the exception explicit and logged, not silent.
- Config change audit log: every `createDraft`/`activate` call writes an
  immutable audit record (could reuse `RiskModel` version history itself as the
  audit trail, since versions are already immutable and timestamped — evaluate
  whether a separate audit table is needed or if this is redundant).

**Exit criterion:** an activation attempt by the same user who created the
draft is rejected (absent an explicit sandbox override); every model version
transition is queryable after the fact with who/when.

---

## Phase 9 — Re-assessment & ongoing monitoring

**Goal:** §5.3's event-driven + periodic re-assessment.

- Event triggers: new screening hit, scored-attribute change, registry-status
  change → each calls `AssessmentService.runAssessment` with the appropriate
  `triggeredBy`. This phase is mostly about wiring existing event sources (or
  stubbing them if they don't exist yet) to the Phase 4 service — the scoring
  path itself doesn't change.
- Periodic: scheduled job keyed to current risk level (High: 6mo, Medium: 12mo,
  Low: 24–36mo). `Tenant`-configurable cadence, not hardcoded.
- "Current risk level" = the level of the customer's latest `RiskAssessment` —
  no separate mutable "current status" field to keep in sync; derive it by
  query (`ORDER BY createdAt DESC LIMIT 1`), which also sidesteps a whole class
  of consistency bugs.

**Exit criterion:** a scheduled job run produces re-assessments for customers
past their review cadence without manual triggering, and each is indistinguishable in structure from an event-triggered or manual one.

---

## Phase 10 — Testing strategy summary

- **`risk.engine`**: pure unit tests, no Spring context, table-driven where
  possible (one test per `matchType`, one per `rollupMethod`, one per
  `combineMethod`, the §8.3 worked example as a golden test).
- **`risk.config`**: validator unit tests, one per rejection rule in §2.2, plus
  round-trip tests (config → entities → config produces an equivalent document).
- **`risk.service`**: integration tests against a real (Testcontainers) Postgres
  — versioning/activation semantics and assessment immutability are the two
  things worth the DB-level rigor here.
- **`risk` + `screening` integration (Phase 6)**: the sanctions-gate test is the
  single most important test in the whole plan — a regression here is a
  regulatory failure, not a bug.

---

## Sequencing summary

```
Phase 0 (done: screening/ moved)
   │
   ▼
Phase 1 (data model) ──▶ Phase 2 (config validation) ──▶ Phase 3 (scoring engine)
                                                              │
                                                              ▼
                                                     Phase 4 (service layer)
                                                              │
                                              ┌───────────────┼───────────────┐
                                              ▼               ▼               ▼
                                        Phase 5 (API)   Phase 6 (screening   Phase 7 (currency
                                                          integration)        normalisation)
                                              │               │               │
                                              └───────┬───────┴───────┬───────┘
                                                       ▼               ▼
                                                Phase 8 (governance)  Phase 9 (re-assessment)
                                                       │
                                                       ▼
                                              Phase 10 (test hardening, ongoing)
```

Phases 5, 6, and 7 can proceed in parallel once Phase 4 lands, since they touch
different seams (HTTP layer, screening module, FX/currency) on top of the same
service API. Phase 6 should not be treated as lower-priority just because it's
listed after 5 — sequence it first if resourcing forces a choice.

## Open questions to resolve before/during implementation

1. **FX rate source and staleness tolerance** (Phase 7) — explicitly punted in
   the spec; needs a decision, not a default.
2. **`CONFIRMED_MATCH` threshold definition** (Phase 6.1) — is `score == 1.0`
   the right bar, or should any `match: true` result count regardless of score?
   Needs sign-off from whoever owns the compliance relationship, not an
   engineering guess.
3. **Maker-checker exception for solo/sandbox tenants** (Phase 8) — does this
   product need a sandbox mode at all in v1, or should every tenant require
   two distinct users from day one?
4. **Separate audit table vs. reusing `RiskModel` version history** (Phase 8) —
   likely redundant to build both; decide during Phase 8, not before.
