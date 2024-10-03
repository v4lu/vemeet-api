CREATE TABLE IF NOT EXISTS swiper_user_profiles (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) REFERENCES users(id),
    description text,
    main_image_url text,
    other_images text[],
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE swiper_user_profile_images (
    profile_id BIGINT NOT NULL,
    image_url VARCHAR(255),
    CONSTRAINT fk_profile FOREIGN KEY (profile_id) REFERENCES swiper_user_profiles(id)
);