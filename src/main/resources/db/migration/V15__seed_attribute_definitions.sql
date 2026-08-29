INSERT INTO attribute_definition (id, created_at, updated_at, created_by, is_active, key, display_name, data_type, source_system, description, category_key, customer_type)
VALUES
    (gen_random_uuid()::varchar, now(), now(), 'flyway-seed', true, 'age', 'Age', 'NUMBER', 'ID_VERIFICATION', 'Customer age, derived from verified date of birth', 'basic_information', 'INDIVIDUAL'),
    (gen_random_uuid()::varchar, now(), now(), 'flyway-seed', true, 'pep', 'Politically Exposed Person', 'BOOLEAN', 'SCREENING', 'Whether the customer is flagged as a PEP by the sanctions/PEP screening result', 'screening', 'INDIVIDUAL');
