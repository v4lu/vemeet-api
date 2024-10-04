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