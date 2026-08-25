CREATE TABLE category (
    id             VARCHAR(36)  PRIMARY KEY,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN,

    key            VARCHAR(255),
    weight         NUMERIC(6, 4),
    ordering       INTEGER,
    display_name   VARCHAR(255),
    roll_up_method VARCHAR(50),
    timing         VARCHAR(50),
    risk_model_id  VARCHAR(36) NOT NULL,

    CONSTRAINT fk_category_risk_model FOREIGN KEY (risk_model_id) REFERENCES risk_model (id),
    CONSTRAINT uk_category_risk_model_key UNIQUE (risk_model_id, key)
);
