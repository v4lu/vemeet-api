CREATE TABLE notification_types (
    id serial PRIMARY KEY,
    name varchar(50) UNIQUE NOT NULL
);

INSERT INTO notification_types (name) VALUES
    ('new_follower'),
    ('new_reaction'),
    ('new_comment'),
    ('new_message'),
    ('new_match');

CREATE TABLE notifications (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL REFERENCES users(id),
    notification_type_id int NOT NULL REFERENCES notification_types(id),
    content text NOT NULL,
    is_read boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now()
);

CREATE INDEX idx_notifications_user_id ON notifications (user_id);
CREATE INDEX idx_notifications_created_at ON notifications (created_at);