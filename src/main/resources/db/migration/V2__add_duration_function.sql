-- Add custom function to calculate trip duration in days
-- This function provides a reliable way to calculate duration between two dates
-- PostgreSQL DATE subtraction returns integer days directly
CREATE OR REPLACE FUNCTION calculate_trip_duration(end_date DATE, start_date DATE)
RETURNS INTEGER AS $$
BEGIN
    RETURN (end_date - start_date);
END;
$$ LANGUAGE plpgsql IMMUTABLE;
