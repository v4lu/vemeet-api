CREATE TABLE IF NOT EXISTS users (
    id bigserial PRIMARY KEY,
    username text NOT NULL UNIQUE,
    birthday date NOT NULL,
    aws_cognito_id text NOT NULL UNIQUE,
    created_at timestamp with time zone DEFAULT now(),
    verified boolean DEFAULT false,
    is_private boolean DEFAULT false,
    inbox_locked boolean DEFAULT false,
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
