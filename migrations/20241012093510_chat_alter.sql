-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';

ALTER TABLE chat_assets
    ADD COLUMN chat_id bigint NOT NULL REFERENCES chats(id),
    ADD COLUMN duration_seconds integer,
    ADD COLUMN mime_type text,
    ADD COLUMN IF NOT EXISTS file_path_encrypted_data_key bytea,
    ADD COLUMN IF NOT EXISTS file_path_encryption_version integer,
    DROP COLUMN IF EXISTS encryption_type,
    DROP COLUMN IF EXISTS encryption_iv,
    DROP COLUMN IF EXISTS encrypted_data_key,
    ALTER COLUMN encrypted_file_path TYPE bytea USING encrypted_file_path::bytea;


ALTER TABLE messages
    ADD COLUMN content_preview text;


ALTER TABLE messages
ALTER COLUMN message_type TYPE varchar(25);

CREATE INDEX idx_chat_assets_chat_id ON chat_assets (chat_id);

-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
-- +goose StatementEnd
