-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
CREATE TABLE IF NOT EXISTS users (
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

CREATE TABLE IF NOT EXISTS images (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    url text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);


CREATE TABLE IF NOT EXISTS followers (
    id bigserial PRIMARY KEY,
    follower_id bigint NOT NULL REFERENCES users(id),
    followed_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follower_pair UNIQUE (follower_id, followed_id)
);

CREATE TABLE IF NOT EXISTS follow_requests (
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
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS images CASCADE;
DROP TABLE IF EXISTS followers CASCADE;
DROP TABLE IF EXISTS follow_requests CASCADE;

DROP INDEX IF EXISTS idx_users_aws_cognito_id;
DROP INDEX IF EXISTS idx_users_username;
DROP INDEX IF EXISTS idx_images_user_id;
DROP INDEX IF EXISTS idx_followers_follower_id;
DROP INDEX IF EXISTS idx_followers_followed_id;
DROP INDEX IF EXISTS idx_followers_follower_followed;
DROP INDEX IF EXISTS idx_follow_requests_requester_id;
DROP INDEX IF EXISTS idx_follow_requests_target_id;
DROP INDEX IF EXISTS idx_follow_requests_requester_target;
DROP INDEX IF EXISTS idx_follow_requests_target_requester;
DROP INDEX IF EXISTS idx_follow_requests_requester_id_target_id;
-- +goose StatementEnd
