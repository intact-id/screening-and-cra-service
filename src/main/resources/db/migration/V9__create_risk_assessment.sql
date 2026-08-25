CREATE TABLE risk_assessment (
    id                VARCHAR(36)  PRIMARY KEY,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    is_active         BOOLEAN,

    tenant_id         VARCHAR(36) NOT NULL,
    customer_id       VARCHAR(255) NOT NULL,
    customer_type     VARCHAR(50),
    risk_model_id     VARCHAR(36) NOT NULL,
    model_version     INTEGER,
    stage             VARCHAR(50),
    status            VARCHAR(50),
    overall_score     NUMERIC(6, 2),
    overall_level     VARCHAR(50),
    override_applied  BOOLEAN,
    input_snapshot    JSONB,
    fx_snapshot       JSONB,

    CONSTRAINT fk_risk_assessment_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id),
    CONSTRAINT fk_risk_assessment_risk_model FOREIGN KEY (risk_model_id) REFERENCES risk_model (id)
);

-- RiskAssessment is append-only (see cra-data-model.md §6/§4.1) — this index is
-- what "current risk level = latest completed assessment" is queried against.
CREATE INDEX idx_risk_assessment_customer ON risk_assessment (tenant_id, customer_id, created_at DESC);
