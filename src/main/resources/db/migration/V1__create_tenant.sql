CREATE TABLE tenant (
    id                 VARCHAR(36)  PRIMARY KEY,
    created_at         TIMESTAMP,
    updated_at         TIMESTAMP,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),
    is_active          BOOLEAN,

    tenant_identifier  VARCHAR(255),
    tenant_type        VARCHAR(50),
    tenant_name        VARCHAR(255),
    description        TEXT,
    extra_details      JSONB
);
