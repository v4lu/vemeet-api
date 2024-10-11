-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
ALTER TABLE review_images DROP CONSTRAINT review_images_review_id_key;
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
-- +goose StatementEnd
