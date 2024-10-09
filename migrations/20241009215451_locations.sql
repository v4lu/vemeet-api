-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
CREATE TABLE IF NOT EXISTS vegan_locations (
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL,
    description text,
    address text NOT NULL,
    city varchar(100) NOT NULL,
    country varchar(100) NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    type varchar(50) NOT NULL,
    website_url varchar(255),
    phone_number varchar(20),
    opening_hours text,
    price_range varchar(20),
    user_id bigint REFERENCES users(id),
    is_verified boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS location_images (
    id bigserial PRIMARY KEY,
    location_id bigint NOT NULL REFERENCES vegan_locations(id) ON DELETE CASCADE,
    image_url text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS location_reviews (
    id bigserial PRIMARY KEY,
    location_id bigint NOT NULL REFERENCES vegan_locations(id) ON DELETE CASCADE,
    user_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    rating integer NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS review_images (
    id bigserial PRIMARY KEY,
    review_id bigint NOT NULL REFERENCES location_reviews(id) ON DELETE CASCADE,
    image_url text NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    UNIQUE (review_id)
);

CREATE INDEX idx_location_images_location_id ON location_images (location_id);
CREATE INDEX idx_location_reviews_location_id ON location_reviews (location_id);
CREATE INDEX idx_location_reviews_user_id ON location_reviews (user_id);
CREATE INDEX idx_review_images_review_id ON review_images (review_id);
CREATE INDEX idx_vegan_locations_type ON vegan_locations (type);
CREATE INDEX idx_vegan_locations_city ON vegan_locations (city);
CREATE INDEX idx_vegan_locations_name ON vegan_locations (name);
CREATE INDEX idx_vegan_locations_user_id ON vegan_locations (user_id);
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
DROP TABLE IF EXISTS review_images 
DROP TABLE IF EXISTS location_reviews
DROP TABLE IF EXISTS location_images
DROP TABLE IF EXISTS vegan_locations
DROP INDEX IF EXISTS idx_location_images_location_id
DROP INDEX IF EXISTS idx_location_reviews_location_id
DROP INDEX IF EXISTS idx_location_reviews_user_id
DROP INDEX IF EXISTS idx_review_images_review_id
DROP INDEX IF EXISTS idx_vegan_locations_type
DROP INDEX IF EXISTS idx_vegan_locations_city
DROP INDEX IF EXISTS idx_vegan_locations_name
DROP INDEX IF EXISTS idx_vegan_locations_user_id
-- +goose StatementEnd

