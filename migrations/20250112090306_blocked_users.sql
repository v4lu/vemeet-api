-- +goose Up
-- +goose StatementBegin
SELECT
  'up SQL query';

ALTER TABLE users
ADD COLUMN blocked BOOLEAN DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS blocked_users (
  id bigserial PRIMARY KEY,
  user_id bigint NOT NULL REFERENCES users (id),
  reason text NOT NULL,
  created_at timestamp
  with
    time zone DEFAULT now ()
);

-- +goose StatementEnd
-- +goose Down
-- +goose StatementBegin
SELECT
  'down SQL query';

-- +goose StatementEnd
