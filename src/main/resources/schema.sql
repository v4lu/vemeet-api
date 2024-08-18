CREATE TABLE IF NOT EXISTS users (
    id bigserial PRIMARY KEY,
    username text NOT NULL UNIQUE,
    birthday date NOT NULL,
    aws_cognito_id text NOT NULL UNIQUE,
    created_at timestamp with time zone DEFAULT now(),
    initial_setup boolean DEFAULT false,
    verified boolean DEFAULT false,
    is_private boolean DEFAULT false,
    inbox_locked boolean DEFAULT false,
    name text,
    gender boolean,
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

CREATE INDEX idx_users_aws_cognito_id ON users (aws_cognito_id);
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_images_user_id ON images (user_id);