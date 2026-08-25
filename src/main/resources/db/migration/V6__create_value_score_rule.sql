CREATE TABLE value_score_rule (
    id            VARCHAR(36)  PRIMARY KEY,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    is_active     BOOLEAN,

    attribute_id  VARCHAR(36) NOT NULL,
    match_type    VARCHAR(50),
    match_value   JSONB,
    priority      INTEGER,
    score         INTEGER,

    CONSTRAINT fk_value_score_rule_attribute FOREIGN KEY (attribute_id) REFERENCES attribute (id)
);
