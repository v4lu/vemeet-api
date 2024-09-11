ALTER TABLE users
    DROP COLUMN birthplace_lat,
    DROP COLUMN birthplace_lng,
    DROP COLUMN birthplace_name,
    DROP COLUMN residence_lat,
    DROP COLUMN residence_lng,
    DROP COLUMN residence_name;

ALTER TABLE users
    ADD COLUMN country_name text,
    ADD COLUMN country_flag text,
    ADD COLUMN country_iso_code text,
    ADD COLUMN country_lat double precision,
    ADD COLUMN country_lng double precision,
    ADD COLUMN city_name text,
    ADD COLUMN city_lat double precision,
    ADD COLUMN city_lng double precision;


