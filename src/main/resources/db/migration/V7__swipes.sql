
CREATE TABLE IF NOT EXISTS user_preferences (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    min_age int,
    max_age int,
    preferred_gender text,
    max_distance int,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_user_preference UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS swipes (
    id bigserial PRIMARY KEY,
    swiper_id bigint NOT NULL REFERENCES users(id),
    swiped_id bigint NOT NULL REFERENCES users(id),
    direction text NOT NULL CHECK (direction IN ('left', 'right')),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_swipe UNIQUE (swiper_id, swiped_id)
);

CREATE TABLE IF NOT EXISTS matches (
    id bigserial PRIMARY KEY,
    user1_id bigint NOT NULL REFERENCES users(id),
    user2_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_match UNIQUE (user1_id, user2_id)
);

CREATE INDEX IF NOT EXISTS idx_user_preferences_user_id ON user_preferences (user_id);
CREATE INDEX IF NOT EXISTS idx_swipes_swiper_id ON swipes (swiper_id);
CREATE INDEX IF NOT EXISTS idx_swipes_swiped_id ON swipes (swiped_id);
CREATE INDEX IF NOT EXISTS idx_matches_user1_id ON matches (user1_id);
CREATE INDEX IF NOT EXISTS idx_matches_user2_id ON matches (user2_id);

-- function to calculate distance between two points (using Haversine formula)
CREATE OR REPLACE FUNCTION calculate_distance(lat1 float, lon1 float, lat2 float, lon2 float)
    RETURNS float AS $$
DECLARE
    x float = 69.1 * (lat2 - lat1);
    y float = 69.1 * (lon2 - lon1) * cos(lat1 / 57.3);
BEGIN
    RETURN sqrt(x * x + y * y);
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE VIEW potential_matches AS
SELECT
    u.id AS user_id,
    p.id AS potential_match_id,
    calculate_distance(u.city_lat, u.city_lng, p.city_lat, p.city_lng) AS distance
FROM
    users u
        CROSS JOIN users p
        JOIN user_preferences up ON u.id = up.user_id
WHERE
    u.id != p.id
  AND (up.preferred_gender = 'Any' OR p.gender = up.preferred_gender)
  AND EXTRACT(YEAR FROM AGE(p.birthday)) BETWEEN up.min_age AND up.max_age
  AND u.city_lat IS NOT NULL
  AND u.city_lng IS NOT NULL
  AND p.city_lat IS NOT NULL
  AND p.city_lng IS NOT NULL
  AND calculate_distance(u.city_lat, u.city_lng, p.city_lat, p.city_lng) <= up.max_distance
  AND NOT EXISTS (
    SELECT 1 FROM swipes s
    WHERE s.swiper_id = u.id AND s.swiped_id = p.id
);


CREATE TABLE IF NOT EXISTS swiper_user_profiles (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) REFERENCES users(id),
    description text,
    main_image_url text,
    other_images text[],
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE swiper_user_profile_images (
    profile_id BIGINT NOT NULL,
    image_url VARCHAR(255),
    CONSTRAINT fk_profile FOREIGN KEY (profile_id) REFERENCES swiper_user_profiles(id)
);