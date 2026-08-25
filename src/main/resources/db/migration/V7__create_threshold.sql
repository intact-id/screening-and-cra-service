CREATE TABLE threshold (
    id             VARCHAR(36)  PRIMARY KEY,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN,

    category_id    VARCHAR(36),
    risk_model_id  VARCHAR(36),
    min_score      NUMERIC(6, 2),
    max_score      NUMERIC(6, 2),
    owner_type     VARCHAR(50),
    risk_level     VARCHAR(50),

    CONSTRAINT fk_threshold_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_threshold_risk_model FOREIGN KEY (risk_model_id) REFERENCES risk_model (id),
    -- Mirrors ThresholdOwnerValidator: exactly one of category_id/risk_model_id
    -- may be set, and it must agree with owner_type. Belt-and-braces — the
    -- @PrePersist/@PreUpdate listener already enforces this at the app layer.
    CONSTRAINT chk_threshold_exactly_one_owner CHECK (
        (category_id IS NOT NULL AND risk_model_id IS NULL AND owner_type = 'CATEGORY')
        OR
        (risk_model_id IS NOT NULL AND category_id IS NULL AND owner_type = 'MODEL')
    )
);
