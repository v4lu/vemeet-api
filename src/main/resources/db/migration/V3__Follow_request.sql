CREATE TABLE IF NOT EXISTS follow_requests (
    id bigserial PRIMARY KEY,
    requester_id bigint NOT NULL REFERENCES users(id),
    target_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follow_request_pair UNIQUE (requester_id, target_id)
);

CREATE INDEX IF NOT EXISTS idx_follow_requests_requester_id ON follow_requests (requester_id);
CREATE INDEX IF NOT EXISTS idx_follow_requests_target_id ON follow_requests (target_id);