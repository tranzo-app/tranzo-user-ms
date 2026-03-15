-- Store Splitwise group id on trip when trip is published and split group is created.
ALTER TABLE core_trip_details
    ADD COLUMN splitwise_group_id BIGINT NULL;
