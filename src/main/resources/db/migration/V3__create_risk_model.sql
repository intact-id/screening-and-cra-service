CREATE TABLE risk_model (
    id                VARCHAR(36)  PRIMARY KEY,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    is_active         BOOLEAN,

    version           INTEGER,
    description       TEXT,
    customer_type     VARCHAR(50),
    life_cycle_status VARCHAR(50),
    combine_method    VARCHAR(50),
    tenant_id         VARCHAR(36) NOT NULL,

    CONSTRAINT fk_risk_model_tenant FOREIGN KEY (tenant_id) REFERENCES tenant (id),
    -- One RiskModel version is uniquely identified per tenant + customerType.
    -- Only one row for a given (tenant_id, customer_type) may be ACTIVE at a
    -- time — enforced in RiskModelService.activate(), not by this constraint.
    CONSTRAINT uk_risk_model_tenant_customer_type_version UNIQUE (tenant_id, customer_type, version)
);
