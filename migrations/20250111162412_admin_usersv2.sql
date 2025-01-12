-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
ALTER TABLE admin_users ADD COLUMN IF NOT EXISTS verified BOOLEAN DEFAULT FALSE;
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
-- +goose StatementEnd
