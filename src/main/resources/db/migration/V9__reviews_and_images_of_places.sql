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