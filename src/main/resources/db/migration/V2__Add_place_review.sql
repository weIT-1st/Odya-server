CREATE TABLE place_review
(
    place_id     VARCHAR2(400) NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    star_rating  INTEGER       NOT NULL,
    "COMMENT"    VARCHAR2(300),
    created_date DATE          NOT NULL,
    CONSTRAINT pk_place_review PRIMARY KEY (place_id, user_id)
);

ALTER TABLE place_review
    ADD CONSTRAINT FK_PLACE_REVIEW_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
