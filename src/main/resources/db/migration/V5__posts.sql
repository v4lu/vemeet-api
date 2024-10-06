CREATE TABLE IF NOT EXISTS content_types (
    id serial PRIMARY KEY,
    name varchar(50) UNIQUE NOT NULL
);

INSERT INTO content_types (name) VALUES ('post'), ('recipe'), ('comment');

CREATE TABLE IF NOT EXISTS posts (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    content text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

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
    preparation_time interval,
    cooking_time interval,
    servings int,
    difficulty varchar(20) CHECK (difficulty IN ('easy', 'medium', 'hard')),
    category_id bigint REFERENCES recipe_categories(id),
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE ingredients (
    id bigserial PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    name varchar(255) NOT NULL,
    CONSTRAINT fk_recipe FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
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
    reaction_type varchar(20) NOT NULL,
    content_type_id int NOT NULL REFERENCES content_types(id),
    content_id bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_user_content_reaction UNIQUE (user_id, content_type_id, content_id)
);

CREATE INDEX idx_posts_user_id ON posts (user_id);
CREATE INDEX idx_post_images_post_id ON post_images (post_id);
CREATE INDEX idx_post_images_image_id ON post_images (image_id);
CREATE INDEX idx_reactions_user_id ON reactions (user_id);

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

-- trigger function to handle cascading deletes
CREATE OR REPLACE FUNCTION delete_related_reactions()
    RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM reactions
    WHERE content_type_id = (SELECT id FROM content_types WHERE name = TG_TABLE_NAME)
      AND content_id = OLD.id;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

-- triggers to posts and recipes tables
CREATE TRIGGER delete_post_reactions
    BEFORE DELETE ON posts
    FOR EACH ROW
EXECUTE FUNCTION delete_related_reactions();

CREATE TRIGGER delete_recipe_reactions
    BEFORE DELETE ON recipes
    FOR EACH ROW
EXECUTE FUNCTION delete_related_reactions();