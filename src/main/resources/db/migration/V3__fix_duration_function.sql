-- Fix the calculate_trip_duration function
-- PostgreSQL DATE subtraction returns integer directly, no EXTRACT needed
CREATE OR REPLACE FUNCTION calculate_trip_duration(end_date DATE, start_date DATE)
RETURNS INTEGER AS $$
BEGIN
    RETURN (end_date - start_date);
END;
$$ LANGUAGE plpgsql IMMUTABLE;
