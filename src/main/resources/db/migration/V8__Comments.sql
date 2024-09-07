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