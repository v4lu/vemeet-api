-- +goose Up
-- +goose StatementBegin

CREATE TABLE IF NOT EXISTS profile_stories (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_profile_story_title_per_user UNIQUE (user_id)
);

CREATE TABLE IF NOT EXISTS story_groups (
    id bigserial PRIMARY KEY,
    profile_story_id bigint REFERENCES profile_stories(id) ON DELETE CASCADE,
    title text,
    image_url text,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stories (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    story_group_id bigint REFERENCES story_groups(id) ON DELETE SET NULL,
    created_at timestamp with time zone DEFAULT now(),
    expires_at timestamp with time zone NOT NULL,
    view_count int DEFAULT 0,
    CONSTRAINT stories_expires_at_check CHECK (expires_at > created_at)
);

CREATE TABLE IF NOT EXISTS story_assets (
    id bigserial PRIMARY KEY,
    story_id bigint NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    asset_type text NOT NULL CHECK (asset_type IN ('image', 'video')),
    content_type text,
    duration interval, -- (videos only)
    encrypted_file_path bytea,
    file_path_encrypted_data_key bytea,
    file_path_encryption_version integer, 
    width int,
    height int,
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_story_asset UNIQUE (story_id)
);

CREATE TABLE IF NOT EXISTS story_views (
    id bigserial PRIMARY KEY,
    story_id bigint NOT NULL REFERENCES stories(id) ON DELETE CASCADE,
    viewer_id bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    viewed_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_story_view UNIQUE (story_id, viewer_id)
);

CREATE INDEX idx_profile_stories_user_id ON profile_stories(user_id);
CREATE INDEX idx_story_groups_profile_story_id ON story_groups(profile_story_id);
CREATE INDEX idx_stories_user_id ON stories(user_id);
CREATE INDEX idx_stories_story_group_id ON stories(story_group_id);
CREATE INDEX idx_story_assets_story_id ON story_assets(story_id);
CREATE INDEX idx_story_views_story_id ON story_views(story_id);
CREATE INDEX idx_story_views_viewer_id ON story_views(viewer_id);

-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
DROP TABLE IF EXISTS story_views;
DROP TABLE IF EXISTS story_assets;
DROP TABLE IF EXISTS stories;
DROP TABLE IF EXISTS story_groups;
DROP TABLE IF EXISTS profile_stories;
-- +goose StatementEnd
