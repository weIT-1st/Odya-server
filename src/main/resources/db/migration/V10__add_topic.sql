CREATE SEQUENCE topic_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE topic
(
    id   NUMBER(19, 0) NOT NULL,
    word VARCHAR2(90)  NOT NULL,
    CONSTRAINT pk_topic PRIMARY KEY (id)
);

ALTER TABLE topic
    ADD CONSTRAINT topic_unique UNIQUE (word);

CREATE SEQUENCE favorite_topic_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE favorite_topic
(
    id       NUMBER(19, 0) NOT NULL,
    user_id  NUMBER(19, 0) NOT NULL,
    topic_id NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_favorite_topic PRIMARY KEY (id)
);

ALTER TABLE favorite_topic
    ADD CONSTRAINT FavoriteTopic_unique UNIQUE (user_id, topic_id);

ALTER TABLE favorite_topic
    ADD CONSTRAINT FK_FAVORITE_TOPIC_ON_TOPIC FOREIGN KEY (topic_id) REFERENCES topic (id);

ALTER TABLE favorite_topic
    ADD CONSTRAINT FK_FAVORITE_TOPIC_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
