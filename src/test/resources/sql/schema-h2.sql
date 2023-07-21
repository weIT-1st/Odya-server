CREATE TABLE users
(
    id            NUMERIC(19, 0) NOT NULL,
    username      VARCHAR2(50)   NOT NULL,
    email         VARCHAR2(255)  NULL,
    nickname      VARCHAR2(24)   NOT NULL,
    phone_number  VARCHAR2(13)   NULL,
    gender        VARCHAR2(1)    NOT NULL,
    birthday      DATE           NOT NULL,
    user_role     VARCHAR2(255)  NOT NULL,
    social_type   VARCHAR2(10)   NOT NULL,
    profile_id    NUMERIC(19, 0) NULL,
    withdraw_date DATE           NULL,
    created_date  DATE           NOT NULL,
    PRIMARY KEY (id)
);

create sequence users_seq start with 1 increment by 1;

ALTER TABLE users
    ADD CONSTRAINT UK_USERNAME_ON_USERS UNIQUE (username);
ALTER TABLE users
    ADD CONSTRAINT UK_NICKNAME_ON_USERS UNIQUE (nickname);
ALTER TABLE users
    ADD CONSTRAINT UK_EMAIL_ON_USERS UNIQUE (email);
ALTER TABLE users
    ADD CONSTRAINT UK_PHONE_NUMBER_ON_USERS UNIQUE (phone_number);

CREATE SEQUENCE place_review_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE place_review
(
    id           NUMBER(19, 0) NOT NULL,
    place_id     VARCHAR2(400) NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    star_rating  INTEGER       NOT NULL,
    review       VARCHAR2(90)  NOT NULL,
    created_date DATE          NOT NULL,
    updated_date DATE          NOT NULL,
    CONSTRAINT pk_placereview PRIMARY KEY (id)
);

ALTER TABLE place_review
    ADD CONSTRAINT place_id_user_id_unique UNIQUE (place_id, user_id);

CREATE INDEX place_id_index ON place_review (place_id);

ALTER TABLE place_review
    ADD CONSTRAINT FK_PLACE_REVIEW_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE follow
(
    follower_id  NUMERIC(19, 0) NOT NULL,
    following_id NUMERIC(19, 0) NOT NULL,
    created_date DATE           NOT NULL,
    CONSTRAINT pk_follow PRIMARY KEY (follower_id, following_id)
);

CREATE TABLE profile
(
    id               NUMERIC(19, 0) NOT NULL,
    profile_name     VARCHAR2(30)   NOT NULL,
    origin_file_name VARCHAR2(255)  NULL,
    profile_color_id NUMERIC(19, 0) NOT NULL,
    created_date     DATE           NOT NULL,
    updated_date     DATE           NOT NULL,
    CONSTRAINT pk_profile PRIMARY KEY (id)
);

create sequence profile_seq start with 1 increment by 1;

CREATE TABLE profile_color
(
    id           NUMERIC(19, 0) NOT NULL,
    color_hex    VARCHAR2(7)    NOT NULL,
    red          INTEGER        NULL,
    green        INTEGER        NOT NULL,
    blue         INTEGER        NOT NULL,
    created_date DATE           NOT NULL,
    CONSTRAINT pk_profile_color PRIMARY KEY (id)
);

create sequence profile_color_seq start with 1 increment by 1;

alter table profile
    add constraint FK_PROFILE_ON_PROFILE_COLOR foreign key (profile_color_id) references profile_color (id);

alter table users
    add constraint FK_USERS_ON_PROFILE foreign key (profile_id) references profile (id);

CREATE SEQUENCE interest_place_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE interest_place
(
    id           NUMBER(19, 0) NOT NULL,
    created_date TIMESTAMP     NOT NULL,
    place_id     VARCHAR2(400) NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_interest_place PRIMARY KEY (id)
);

ALTER TABLE interest_place
    ADD CONSTRAINT interest_place_id_user_id_unique UNIQUE (place_id, user_id);

CREATE INDEX interest_place_id_index ON interest_place (place_id);

ALTER TABLE interest_place
    ADD CONSTRAINT FK_INTEREST_PLACE_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
