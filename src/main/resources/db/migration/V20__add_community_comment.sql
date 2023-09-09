CREATE TABLE community_comment
(
    id              NUMBER(19, 0) NOT NULL,
    content         VARCHAR2(300) NOT NULL,
    created_date    TIMESTAMP     NOT NULL,
    updated_date    TIMESTAMP     NOT NULL,
    user_id         NUMBER(19, 0) NOT NULL,
    community_id    NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_community_comment PRIMARY KEY (id)
);

CREATE SEQUENCE community_comment_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE community_comment
    ADD CONSTRAINT FK_COMMUNITY_COMMENT_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE community_comment
    ADD CONSTRAINT FK_COMMUNITY_COMMENT_ON_COMMUNITY FOREIGN KEY (community_id) REFERENCES community (id);

CREATE INDEX community_index ON community_comment (community_id);
