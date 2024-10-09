-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
CREATE TABLE IF NOT EXISTS countries (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    flag TEXT,
    iso_code TEXT UNIQUE NOT NULL,
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    lat DOUBLE PRECISION,
    lng DOUBLE PRECISION,
    coutry_id INTEGER REFERENCES countries(id)
);

CREATE INDEX idx_country_iso_code ON countries(iso_code);
CREATE INDEX idx_city_country_id ON cities(id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
DROP TABLE IF EXISTS countries CASCADE;
DROP TABLE IF EXISTS cities CASCADE;

DROP INDEX IF EXISTS idx_country_iso_code;
DROP INDEX IF EXISTS idx_city_country_id;
-- +goose StatementEnd
