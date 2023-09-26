ALTER TABLE community
    ADD like_count INTEGER DEFAULT 0 NOT NULL;

CREATE INDEX community_like_count_index ON community (like_count);
