CREATE TABLE attribute (
    id                        VARCHAR(36)  PRIMARY KEY,
    created_at                TIMESTAMP,
    updated_at                TIMESTAMP,
    created_by                VARCHAR(255),
    updated_by                VARCHAR(255),
    is_active                 BOOLEAN,

    attribute_definition_key  VARCHAR(255) NOT NULL,
    weight                    NUMERIC(6, 4),
    is_required               BOOLEAN,
    default_score             INTEGER,
    missing_behavior          VARCHAR(50),
    category_id               VARCHAR(36) NOT NULL,

    CONSTRAINT fk_attribute_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_attribute_definition FOREIGN KEY (attribute_definition_key) REFERENCES attribute_definition (key)
);
