CREATE TABLE community_like
(
    community_id NUMERIC(19, 0) NOT NULL,
    user_id      NUMERIC(19, 0) NOT NULL,
    created_date DATE           NOT NULL,
    CONSTRAINT pk_community_like PRIMARY KEY (community_id, user_id)
);
