CREATE SEQUENCE favorite_place_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE favorite_place
(
    id           NUMBER(19, 0) NOT NULL,
    created_date TIMESTAMP     NOT NULL,
    place_id     VARCHAR2(400) NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_favorite_place PRIMARY KEY (id)
);

ALTER TABLE favorite_place
    ADD CONSTRAINT favorite_place_id_user_id_unique UNIQUE (place_id, user_id);

CREATE INDEX favorite_place_id_index ON favorite_place (place_id);

ALTER TABLE favorite_place
    ADD CONSTRAINT FK_FAVORITE_PLACE_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
