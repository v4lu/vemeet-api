-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
ALTER TABLE story_assets
DROP CONSTRAINT IF EXISTS story_assets_asset_type_check,
ADD CONSTRAINT story_assets_asset_type_check 
    CHECK (asset_type IN ('IMAGE', 'VIDEO'));
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
-- +goose StatementEnd
