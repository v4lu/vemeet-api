-- +goose Up
-- +goose StatementBegin
SELECT 'up SQL query';
ALTER TABLE swipes DROP CONSTRAINT unique_swipe;
-- +goose StatementEnd

-- +goose Down
-- +goose StatementBegin
SELECT 'down SQL query';
ALTER TABLE swipes ADD CONSTRAINT unique_swipe UNIQUE (swiper_id, swiped_id);
-- +goose StatementEnd
