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
