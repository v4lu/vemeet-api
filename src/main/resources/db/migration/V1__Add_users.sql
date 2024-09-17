CREATE TABLE IF NOT EXISTS users (
    id bigserial PRIMARY KEY,
    username text NOT NULL UNIQUE,
    birthday date NOT NULL,
    aws_cognito_id text NOT NULL UNIQUE,
    created_at timestamp with time zone DEFAULT now(),
    verified boolean DEFAULT false,
    is_private boolean DEFAULT false,
    inbox_locked boolean DEFAULT false,
    swiper_mode BOOLEAN NOT NULL DEFAULT false,
    name text,
    gender text,
    birthplace_lat double precision,
    birthplace_lng double precision,
    birthplace_name text,
    residence_lat double precision,
    residence_lng double precision,
    residence_name text,
    bio text,
    profile_image_id bigint
    );

CREATE TABLE IF NOT EXISTS images (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    url text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);

ALTER TABLE users
ADD CONSTRAINT fk_users_profile_image
FOREIGN KEY (profile_image_id) REFERENCES images(id);

CREATE TABLE IF NOT EXISTS followers (
    id bigserial PRIMARY KEY,
    follower_id bigint NOT NULL REFERENCES users(id),
    followed_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follower_pair UNIQUE (follower_id, followed_id)
    );

CREATE INDEX IF NOT EXISTS idx_users_aws_cognito_id ON users (aws_cognito_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_images_user_id ON images (user_id);
CREATE INDEX IF NOT EXISTS idx_followers_follower_id ON followers (follower_id);
CREATE INDEX IF NOT EXISTS idx_followers_followed_id ON followers (followed_id);
CREATE INDEX IF NOT EXISTS idx_followers_follower_followed ON followers (follower_id, followed_id);

CREATE TABLE IF NOT EXISTS follow_requests (
    id bigserial PRIMARY KEY,
    requester_id bigint NOT NULL REFERENCES users(id),
    target_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follow_request_pair UNIQUE (requester_id, target_id)
);

CREATE INDEX IF NOT EXISTS idx_follow_requests_requester_id ON follow_requests (requester_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_target_id ON follow_requests (target_id);


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

CREATE INDEX idx_vegan_locations_type ON vegan_locations (type);
CREATE INDEX idx_vegan_locations_city ON vegan_locations (city);
CREATE INDEX idx_vegan_locations_name ON vegan_locations (name);
CREATE INDEX idx_vegan_locations_user_id ON vegan_locations (user_id);

CREATE TABLE recipe_categories (
    id bigserial PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE recipes (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    title varchar(255) NOT NULL,
    content jsonb,
    instructions text NOT NULL,
    ingredients text[] NOT NULL,
    preparation_time interval,
    cooking_time interval,
    servings int,
    difficulty varchar(20) CHECK (difficulty IN ('easy', 'medium', 'hard')),
    category_id bigint REFERENCES recipe_categories(id),
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE recipe_images (
    id bigserial PRIMARY KEY,
    recipe_id bigint NOT NULL REFERENCES recipes(id),
    image_url text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


CREATE TABLE tags (
    id bigserial PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE recipe_tags (
    recipe_id bigint REFERENCES recipes(id),
    tag_id bigint REFERENCES tags(id),
    PRIMARY KEY (recipe_id, tag_id)
);


CREATE TABLE IF NOT EXISTS posts (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    content text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS post_images (
    id bigserial PRIMARY KEY,
    post_id bigint NOT NULL REFERENCES posts(id),
    image_id bigint NOT NULL REFERENCES images(id),
    order_index int NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS reactions (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    post_id bigint NOT NULL REFERENCES posts(id),
    reaction_type varchar(20) NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_user_post_reaction UNIQUE (user_id, post_id)
);

CREATE INDEX idx_posts_user_id ON posts (user_id);
CREATE INDEX idx_post_images_post_id ON post_images (post_id);
CREATE INDEX idx_post_images_image_id ON post_images (image_id);
CREATE INDEX idx_reactions_user_id ON reactions (user_id);
CREATE INDEX idx_reactions_post_id ON reactions (post_id);

CREATE TABLE comments (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    content text NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    parent_id bigint REFERENCES comments(id),
    post_id bigint REFERENCES posts(id),
    recipe_id bigint REFERENCES recipes(id),
    CONSTRAINT check_parent_or_post_or_recipe CHECK (
    (parent_id IS NOT NULL AND post_id IS NULL AND recipe_id IS NULL) OR
    (parent_id IS NULL AND post_id IS NOT NULL AND recipe_id IS NULL) OR
    (parent_id IS NULL AND post_id IS NULL AND recipe_id IS NOT NULL)
    )
);

CREATE TABLE comment_reactions (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    comment_id bigint NOT NULL REFERENCES comments(id),
    reaction_type varchar(20) NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_user_comment_reaction UNIQUE (user_id, comment_id)
);

CREATE INDEX idx_comment_reactions_user_id ON comment_reactions (user_id);
CREATE INDEX idx_comment_reactions_comment_id ON comment_reactions (comment_id);


CREATE INDEX idx_comments_user_id ON comments (user_id);
CREATE INDEX idx_comments_parent_id ON comments (parent_id);
CREATE INDEX idx_comments_post_id ON comments (post_id);
CREATE INDEX idx_comments_recipe_id ON comments (recipe_id);

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

ALTER TABLE review_images
DROP CONSTRAINT review_images_review_id_key;


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


CREATE TABLE IF NOT EXISTS chats (
    id bigserial PRIMARY KEY,
    user1_id bigint NOT NULL REFERENCES users(id),
    user2_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_chat_pair UNIQUE (user1_id, user2_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id bigserial PRIMARY KEY,
    chat_id bigint NOT NULL REFERENCES chats(id),
    sender_id bigint NOT NULL REFERENCES users(id),
    message_type text NOT NULL,
    encrypted_content bytea,
    encryption_type text NOT NULL,
    encryption_iv bytea,
    encrypted_data_key bytea,
    created_at timestamp with time zone DEFAULT now(),
    read_at timestamp with time zone,
    is_one_time boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS chat_assets (
    id bigserial PRIMARY KEY,
    message_id bigint NOT NULL REFERENCES messages(id),
    file_type text NOT NULL,
    file_size bigint NOT NULL,
    encrypted_file_path text NOT NULL,
    encryption_type text NOT NULL,
    encryption_iv bytea,
    encrypted_data_key bytea,
    created_at timestamp with time zone DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_chats_user1_id ON chats (user1_id);
CREATE INDEX IF NOT EXISTS idx_chats_user2_id ON chats (user2_id);
CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON messages (chat_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages (sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages (created_at);
CREATE INDEX IF NOT EXISTS idx_chat_assets_message_id ON chat_assets (message_id);


ALTER TABLE chats
    ADD COLUMN last_message_id bigint REFERENCES messages(id),
    ADD COLUMN user1_seen_status boolean DEFAULT false,
    ADD COLUMN user2_seen_status boolean DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_chats_last_message_id ON chats (last_message_id);

ALTER TABLE messages
    ALTER COLUMN encryption_type DROP NOT NULL;

-- trigger to update last_message_id and reset seen status
CREATE OR REPLACE FUNCTION update_chat_last_message() RETURNS TRIGGER AS $$
BEGIN
    UPDATE chats
    SET last_message_id = NEW.id,
        user1_seen_status = CASE WHEN NEW.sender_id = user1_id THEN true ELSE false END,
        user2_seen_status = CASE WHEN NEW.sender_id = user2_id THEN true ELSE false END,
        updated_at = NOW()
    WHERE id = NEW.chat_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_chat_last_message
    AFTER INSERT ON messages
    FOR EACH ROW
EXECUTE FUNCTION update_chat_last_message();


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