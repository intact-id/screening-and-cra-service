CREATE TABLE contribution_line (
    id                VARCHAR(36)  PRIMARY KEY,
    created_at        TIMESTAMP,
    updated_at        TIMESTAMP,
    created_by        VARCHAR(255),
    updated_by        VARCHAR(255),
    is_active         BOOLEAN,

    assessment_id     VARCHAR(36) NOT NULL,
    category_key      VARCHAR(255),
    attribute_key     VARCHAR(255),
    raw_value         JSONB,
    normalized_value  JSONB,
    matched_rule_id   VARCHAR(36),
    points            INTEGER,
    weight_applied    NUMERIC(6, 4),
    weighted_points   NUMERIC(10, 4),
    note              VARCHAR(500),

    CONSTRAINT fk_contribution_line_assessment FOREIGN KEY (assessment_id) REFERENCES risk_assessment (id),
    CONSTRAINT fk_contribution_line_matched_rule FOREIGN KEY (matched_rule_id) REFERENCES value_score_rule (id)
);
