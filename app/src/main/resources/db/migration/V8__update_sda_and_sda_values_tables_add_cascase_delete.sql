ALTER TABLE service_definition_attributes
    DROP FOREIGN KEY service_definition_attributes_ibfk_1;

ALTER TABLE service_definition_attributes
    ADD CONSTRAINT fk_service_definition_attributes_services
        FOREIGN KEY (service_id)
            REFERENCES services (id)
            ON DELETE CASCADE;


ALTER TABLE service_definition_attribute_values
    DROP FOREIGN KEY service_definition_attribute_values_ibfk_1;

ALTER TABLE service_definition_attribute_values
    ADD CONSTRAINT fk_service_definition_attribute_values_services
        FOREIGN KEY (service_definition_attribute_id)
            REFERENCES service_definition_attributes (id)
            ON DELETE CASCADE;