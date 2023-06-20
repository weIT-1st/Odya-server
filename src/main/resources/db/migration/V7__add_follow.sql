CREATE TABLE follow
(
    follower_id  NUMERIC(19, 0) NOT NULL,
    following_id NUMERIC(19, 0) NOT NULL,
    created_date DATE           NOT NULL,
    CONSTRAINT pk_follow PRIMARY KEY (follower_id, following_id)
);