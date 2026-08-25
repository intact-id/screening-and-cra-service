-- Platform-owned catalog: no tenant/model FK, seeded via migration or an
-- internal admin process, never created by a tenant. See cra-data-model.md §2.1.
CREATE TABLE attribute_definition (
    id            VARCHAR(36)  PRIMARY KEY,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    created_by    VARCHAR(255),
    updated_by    VARCHAR(255),
    is_active     BOOLEAN,

    key           VARCHAR(255) NOT NULL,
    display_name  VARCHAR(255),
    data_type     VARCHAR(50),
    source_system VARCHAR(50),
    description   TEXT,
    category_key  VARCHAR(255),
    customer_type VARCHAR(50),

    CONSTRAINT uk_attribute_definition_key UNIQUE (key)
);
