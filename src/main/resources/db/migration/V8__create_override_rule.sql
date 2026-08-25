CREATE TABLE override_rule (
    id             VARCHAR(36)  PRIMARY KEY,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN,

    force_level    VARCHAR(50),
    condition      JSONB,
    reason         VARCHAR(255),
    risk_model_id  VARCHAR(36) NOT NULL,

    CONSTRAINT fk_override_rule_risk_model FOREIGN KEY (risk_model_id) REFERENCES risk_model (id),
    -- An override can only raise the level, never lower it (see cra-data-model.md §3.7) —
    -- LOW is never a valid forceLevel.
    CONSTRAINT chk_override_rule_force_level CHECK (force_level IN ('MEDIUM', 'HIGH'))
);
