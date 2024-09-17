ALTER TABLE chats
    ADD COLUMN last_message_id bigint REFERENCES messages(id),
    ADD COLUMN user1_seen_status boolean DEFAULT false,
    ADD COLUMN user2_seen_status boolean DEFAULT false;

CREATE INDEX IF NOT EXISTS idx_chats_last_message_id ON chats (last_message_id);

ALTER TABLE messages
    ALTER COLUMN encryption_type DROP NOT NULL;

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
