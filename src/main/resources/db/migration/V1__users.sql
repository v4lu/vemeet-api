CREATE TABLE users (
    id bigserial PRIMARY KEY,
    username text NOT NULL UNIQUE,
    birthday timestamp NOT NULL,
    aws_cognito_id text NOT NULL UNIQUE,
    created_at timestamp with time zone DEFAULT now(),
    verified boolean DEFAULT false,
    is_private boolean DEFAULT false,
    inbox_locked boolean DEFAULT false,
    swiper_mode BOOLEAN NOT NULL DEFAULT false,
    name text,
    gender text,
    country_name text,
    country_flag text,
    country_iso_code text,
    country_lat double precision,
    country_lng double precision,
    city_name text,
    city_lat double precision,
    city_lng double precision,
    bio text,
    profile_image_id bigint
);

CREATE TABLE images (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    url text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);

CREATE TABLE followers (
    id bigserial PRIMARY KEY,
    follower_id bigint NOT NULL REFERENCES users(id),
    followed_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follower_pair UNIQUE (follower_id, followed_id)
);

CREATE TABLE follow_requests (
    id bigserial PRIMARY KEY,
    requester_id bigint NOT NULL REFERENCES users(id),
    target_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follow_request_pair UNIQUE (requester_id, target_id)
);



ALTER TABLE users
ADD CONSTRAINT fk_users_profile_image
FOREIGN KEY (profile_image_id) REFERENCES images(id);

CREATE INDEX IF NOT EXISTS idx_users_aws_cognito_id ON users (aws_cognito_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_images_user_id ON images (user_id);
CREATE INDEX IF NOT EXISTS idx_followers_follower_id ON followers (follower_id);
CREATE INDEX IF NOT EXISTS idx_followers_followed_id ON followers (followed_id);
CREATE INDEX IF NOT EXISTS idx_followers_follower_followed ON followers (follower_id, followed_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_requester_id ON follow_requests (requester_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_target_id ON follow_requests (target_id);