INSERT INTO attribute_definition (id, created_at, updated_at, created_by, is_active, key, display_name, data_type, source_system, description, category_key, customer_type)
VALUES
    (gen_random_uuid()::varchar, now(), now(), 'flyway-seed', true, 'country_of_residence', 'Country of Residence', 'COUNTRY', 'MANUAL', 'Customer-declared country of residence', 'basic_information', 'INDIVIDUAL'),
    (gen_random_uuid()::varchar, now(), now(), 'flyway-seed', true, 'product_tier', 'Product Tier', 'ENUM', 'MANUAL', 'Which product tier the customer is onboarding into', 'product', 'BOTH'),
    (gen_random_uuid()::varchar, now(), now(), 'flyway-seed', true, 'adverse_media', 'Adverse Media', 'BOOLEAN', 'SCREENING', 'Whether adverse media was found in the screening result', 'screening', 'INDIVIDUAL');
