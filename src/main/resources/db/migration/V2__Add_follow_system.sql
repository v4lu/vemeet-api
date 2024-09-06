CREATE TABLE IF NOT EXISTS followers (
    id bigserial PRIMARY KEY,
    follower_id bigint NOT NULL REFERENCES users(id),
    followed_id bigint NOT NULL REFERENCES users(id),
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT unique_follower_pair UNIQUE (follower_id, followed_id)
);

CREATE INDEX IF NOT EXISTS idx_followers_follower_id ON followers (follower_id);
CREATE INDEX IF NOT EXISTS idx_followers_followed_id ON followers (followed_id);
CREATE INDEX IF NOT EXISTS idx_followers_follower_followed ON followers (follower_id, followed_id);

-- Create a materialized view for follower counts
CREATE MATERIALIZED VIEW user_follower_counts AS
SELECT followed_id as user_id, COUNT(*) as follower_count
FROM followers
GROUP BY followed_id;

-- Create a materialized view for following counts
CREATE MATERIALIZED VIEW user_following_counts AS
SELECT follower_id as user_id, COUNT(*) as following_count
FROM followers
GROUP BY follower_id;

-- Create an index on each materialized view
CREATE UNIQUE INDEX ON user_follower_counts (user_id);
CREATE UNIQUE INDEX ON user_following_counts (user_id);

-- Function to refresh materialized views
CREATE OR REPLACE FUNCTION refresh_follower_counts()
    RETURNS TRIGGER AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY user_follower_counts;
    REFRESH MATERIALIZED VIEW CONCURRENTLY user_following_counts;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger to refresh materialized views when followers table changes
CREATE TRIGGER update_follower_counts
    AFTER INSERT OR DELETE OR UPDATE ON followers
    FOR EACH STATEMENT EXECUTE FUNCTION refresh_follower_counts();

-- Prevent self-following (if not already implemented)
CREATE OR REPLACE FUNCTION prevent_self_follow()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.follower_id = NEW.followed_id THEN
        RAISE EXCEPTION 'Users cannot follow themselves';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER check_self_follow
    BEFORE INSERT OR UPDATE ON followers
    FOR EACH ROW EXECUTE FUNCTION prevent_self_follow();