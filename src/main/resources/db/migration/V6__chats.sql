CREATE TABLE IF NOT EXISTS chats (
    id bigserial PRIMARY KEY,
    user1_id bigint NOT NULL REFERENCES users(id),
    user2_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now(),
    user1_seen_status boolean DEFAULT false,
    user2_seen_status boolean DEFAULT false,
    CONSTRAINT unique_chat_pair UNIQUE (user1_id, user2_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id bigserial PRIMARY KEY,
    chat_id bigint NOT NULL REFERENCES chats(id),
    sender_id bigint NOT NULL REFERENCES users(id),
    message_type text NOT NULL,
    encrypted_content bytea,
    encrypted_data_key bytea,
    encryption_version integer,
    created_at timestamp with time zone DEFAULT now(),
    read_at timestamp with time zone,
    is_one_time boolean DEFAULT false
);

CREATE TABLE IF NOT EXISTS chat_assets (
    id bigserial PRIMARY KEY,
    message_id bigint NOT NULL REFERENCES messages(id),
    file_type text NOT NULL,
    file_size bigint NOT NULL,
    encrypted_file_path text NOT NULL,
    encryption_type text,
    encryption_iv bytea,
    encrypted_data_key bytea,
    created_at timestamp with time zone DEFAULT now()
);

ALTER TABLE chats
ADD COLUMN last_message_id bigint REFERENCES messages(id);

CREATE INDEX IF NOT EXISTS idx_chats_user1_id ON chats (user1_id);
CREATE INDEX IF NOT EXISTS idx_chats_user2_id ON chats (user2_id);
CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON messages (chat_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages (sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_created_at ON messages (created_at);
CREATE INDEX IF NOT EXISTS idx_chat_assets_message_id ON chat_assets (message_id);


CREATE INDEX IF NOT EXISTS idx_chats_last_message_id ON chats (last_message_id);


-- trigger to update last_message_id and reset seen status
CREATE OR REPLACE FUNCTION update_chat_last_message() RETURNS TRIGGER AS $$
BEGIN
    UPDATE chats
    SET last_message_id = NEW.id,
        user1_seen_status = CASE WHEN NEW.sender_id = user1_id THEN true ELSE false END,
        user2_seen_status = CASE WHEN NEW.sender_id = user2_id THEN true ELSE false END,
        updated_at = NOW()
    WHERE id = NEW.chat_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_chat_last_message
    AFTER INSERT ON messages
    FOR EACH ROW
EXECUTE FUNCTION update_chat_last_message();