CREATE TABLE users
(
    id            NUMERIC(19, 0) NOT NULL,
    username      VARCHAR2(50)   NOT NULL,
    email         VARCHAR2(255)  NULL,
    nickname      VARCHAR2(24)   NOT NULL,
    phone_number  VARCHAR2(13)   NULL,
    gender        VARCHAR2(1)    NOT NULL,
    birthday      DATE           NOT NULL,
    profile_name  VARCHAR2(255)  NOT NULL,
    social_type   VARCHAR2(10)   NOT NULL,
    withdraw_date DATE           NULL,
    created_date  DATE           NOT NULL,
    PRIMARY KEY (id)
);

create sequence users_seq start with 1 increment by 1;

CREATE TABLE place_review
(
    place_id     VARCHAR2(400) NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    star_rating  INTEGER       NOT NULL,
    "COMMENT"    VARCHAR2(300) NOT NULL,
    created_date TIMESTAMP     NOT NULL,
    updated_date TIMESTAMP,
    CONSTRAINT pk_place_review PRIMARY KEY (place_id, user_id)
);

ALTER TABLE place_review
    ADD CONSTRAINT FK_PLACE_REVIEW_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
