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