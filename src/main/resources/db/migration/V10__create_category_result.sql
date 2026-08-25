CREATE TABLE category_result (
    id             VARCHAR(36)  PRIMARY KEY,
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,
    created_by     VARCHAR(255),
    updated_by     VARCHAR(255),
    is_active      BOOLEAN,

    assessment_id  VARCHAR(36) NOT NULL,
    category_key   VARCHAR(255),
    category_score NUMERIC(6, 2),
    category_level VARCHAR(50),

    CONSTRAINT fk_category_result_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessment (id)
);
