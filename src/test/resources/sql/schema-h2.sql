CREATE TABLE users
(
    id           NUMERIC(19, 0) NOT NULL,
    username     VARCHAR2(50)   NOT NULL,
    email        VARCHAR2(255)  NULL,
    nickname     VARCHAR2(24)   NOT NULL,
    phone_number VARCHAR2(13)   NULL,
    gender       VARCHAR2(1)    NOT NULL,
    birthday     DATE           NOT NULL,
    user_role    VARCHAR2(255)  NOT NULL,
    social_type  VARCHAR2(10)   NOT NULL,
    profile_id   NUMERIC(19, 0) NULL,
    created_date DATE           NOT NULL,
    fcm_token    VARCHAR(255)   NULL,
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
ALTER TABLE users
    ADD CONSTRAINT UK_FCM_TOKEN_ON_USERS UNIQUE (fcm_token);

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

CREATE INDEX place_review_foreign_index ON place_review (user_id);

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

CREATE TABLE travel_journal
(
    id                NUMERIC(19, 0) NOT NULL,
    title             VARCHAR2(60)   NOT NULL,
    travel_start_date DATE           NOT NULL,
    travel_end_date   DATE           NOT NULL,
    visibility        VARCHAR2(20)   NOT NULL,
    created_date      TIMESTAMP      NOT NULL,
    updated_date      TIMESTAMP      NOT NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_travel_journal PRIMARY KEY (id)
);

CREATE TABLE travel_journal_content
(
    id                NUMERIC(19, 0) NOT NULL,
    content           VARCHAR2(600)  NULL,
    place_id          VARCHAR2(400)  NULL,
    coordinates       CLOB           NULL,
    travel_date       DATE           NOT NULL,
    created_date      TIMESTAMP      NOT NULL,
    updated_date      TIMESTAMP      NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_travel_journal_content PRIMARY KEY (id)
);

CREATE TABLE travel_companion
(
    id                NUMERIC(19, 0) NOT NULL,
    username          VARCHAR(255)   NULL,
    created_date      TIMESTAMP      NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    user_id           NUMERIC(19, 0) NULL,
    CONSTRAINT pk_travel_companion PRIMARY KEY (id)
);

CREATE TABLE content_image
(
    id           NUMERIC(19, 0) NOT NULL,
    name         VARCHAR2(30)   NOT NULL,
    origin_name  VARCHAR2(255)  NOT NULL,
    is_life_shot NUMBER(1)      NOT NULL,
    created_date TIMESTAMP      NOT NULL,
    user_id      NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_content_image PRIMARY KEY (id)
);

CREATE TABLE travel_journal_content_image
(
    id                        NUMERIC(19, 0) NOT NULL,
    created_date              TIMESTAMP      NOT NULL,
    content_image_id          NUMERIC(19, 0) NOT NULL,
    travel_journal_content_id NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_travel_journal_content_image PRIMARY KEY (id)
);

CREATE SEQUENCE travel_journal_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE travel_journal_content_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE travel_companion_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE content_image_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE travel_journal_content_image_seq START WITH 1 INCREMENT BY 1;

alter table travel_journal
    add constraint FK_TRAVEL_JOURNAL_ON_USERS foreign key (user_id) references users (id);

alter table travel_journal_content_image
    add constraint FK_TRAVEL_IMAGE_ON_TRAVEL_JOURNAL_CONTENT foreign key (travel_journal_content_id) references travel_journal_content (id);

alter table travel_journal_content_image
    add constraint FK_TRAVEL_IMAGE_ON_CONTENT_IMAGE foreign key (content_image_id) references content_image (id);

alter table content_image
    add constraint FK_CONTENT_IMAGE_ON_USERS foreign key (user_id) references users (id);

alter table travel_journal_content
    add constraint FK_TRAVEL_JOURNAL_CONTENT_ON_TRAVEL_JOURNAL foreign key (travel_journal_id) references travel_journal (id);

alter table travel_companion
    add constraint FK_TRAVEL_COMPANION_ON_TRAVEL_JOURNAL foreign key (travel_journal_id) references travel_journal (id);

alter table travel_companion
    add constraint FK_TRAVEL_COMPANION_ON_USERS foreign key (user_id) references users (id);


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
    id           NUMBER(19, 0) NOT NULL,
    created_date TIMESTAMP     NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    topic_id     NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_favoritetopic PRIMARY KEY (id)
);

ALTER TABLE favorite_topic
    ADD CONSTRAINT FavoriteTopic_unique UNIQUE (user_id, topic_id);

ALTER TABLE favorite_topic
    ADD CONSTRAINT FK_FAVORITE_TOPIC_ON_TOPIC FOREIGN KEY (topic_id) REFERENCES topic (id);

ALTER TABLE favorite_topic
    ADD CONSTRAINT FK_FAVORITE_TOPIC_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE SEQUENCE terms_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE terms
(
    id           NUMBER(19, 0)       NOT NULL,
    updated_date TIMESTAMP           NOT NULL,
    created_date TIMESTAMP           NOT NULL,
    title        VARCHAR2(60)        NOT NULL,
    content      CLOB                NOT NULL,
    required     NUMBER(1) DEFAULT 0 NOT NULL,
    CONSTRAINT pk_terms PRIMARY KEY (id)
);

ALTER TABLE terms
    ADD CONSTRAINT terms_title_unique UNIQUE (title);

CREATE SEQUENCE agreed_terms_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE agreed_terms
(
    id           NUMBER(19, 0) NOT NULL,
    created_date TIMESTAMP     NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    terms_id     NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_agreed_terms PRIMARY KEY (id)
);

ALTER TABLE agreed_terms
    ADD CONSTRAINT agreedTerms_unique UNIQUE (user_id, terms_id);

ALTER TABLE agreed_terms
    ADD CONSTRAINT FK_AGREED_TERMS_ON_TERMS FOREIGN KEY (terms_id) REFERENCES terms (id);

ALTER TABLE agreed_terms
    ADD CONSTRAINT FK_AGREED_TERMS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE TABLE community
(
    id                NUMERIC(19, 0) NOT NULL,
    content           VARCHAR2(600)  NOT NULL,
    visibility        VARCHAR2(20)   NOT NULL,
    place_id          VARCHAR2(400)  NULL,
    created_date      TIMESTAMP      NOT NULL,
    updated_date      TIMESTAMP      NOT NULL,
    topic_id          NUMERIC(19, 0) NULL,
    travel_journal_id NUMERIC(19, 0) NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_community PRIMARY KEY (id)
);

CREATE SEQUENCE community_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE community_content_image
(
    id               NUMERIC(19, 0) NOT NULL,
    created_date     TIMESTAMP      NOT NULL,
    content_image_id NUMERIC(19, 0) NOT NULL,
    community_id     NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_community_content_image PRIMARY KEY (id)
);

CREATE SEQUENCE community_content_image_seq START WITH 1 INCREMENT BY 1;

alter table community
    add constraint FK_COMMUNITY_ON_TOPIC foreign key (topic_id) references topic (id);

alter table community
    add constraint FK_COMMUNITY_ON_TRAVEL_JOURNAL foreign key (travel_journal_id) references travel_journal (id);

alter table community
    add constraint FK_COMMUNITY_ON_USER foreign key (user_id) references users (id);

alter table community_content_image
    add constraint FK_COMMUNITY_CONTENT_IMAGE_ON_COMMUNITY foreign key (community_id) references community (id);

alter table community_content_image
    add constraint FK_COMMUNITY_CONTENT_IMAGE_ON_CONTENT_IMAGE foreign key (content_image_id) references content_image (id);

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

CREATE SEQUENCE report_place_review_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_place_review
(
    id              NUMBER(19, 0) NOT NULL,
    created_date      TIMESTAMP   NOT NULL,
    place_review_id NUMBER(19, 0) NOT NULL,
    user_id         NUMBER(19, 0) NOT NULL,
    report_reason   VARCHAR2(20)  NOT NULL,
    other_reason    VARCHAR2(60),
    CONSTRAINT pk_report_place_review PRIMARY KEY (id)
);

ALTER TABLE report_place_review
    ADD CONSTRAINT report_place_review_unique UNIQUE (user_id, place_review_id);

ALTER TABLE report_place_review
    ADD CONSTRAINT FK_REPORT_PLACE_REVIEW_ON_PLACE_REVIEW FOREIGN KEY (place_review_id) REFERENCES place_review (id);

ALTER TABLE report_place_review
    ADD CONSTRAINT FK_REPORT_PLACE_REVIEW_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX report_place_review_foreign_index ON REPORT_PLACE_REVIEW (place_review_id);
CREATE INDEX report_place_review_foreign_index_2 ON REPORT_PLACE_REVIEW (user_id);

CREATE SEQUENCE report_travel_journal_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_travel_journal
(
    id                NUMBER(19, 0) NOT NULL,
    created_date      TIMESTAMP     NOT NULL,
    travel_journal_id NUMBER(19, 0) NOT NULL,
    user_id           NUMBER(19, 0) NOT NULL,
    report_reason     VARCHAR2(20)  NOT NULL,
    other_reason      VARCHAR2(60),
    CONSTRAINT pk_report_travel_journal PRIMARY KEY (id)
);

ALTER TABLE report_travel_journal
    ADD CONSTRAINT report_travel_journal_unique UNIQUE (travel_journal_id, user_id);

ALTER TABLE report_travel_journal
    ADD CONSTRAINT FK_REPORT_TRAVEL_JOURNAL_ON_TRAVEL_JOURNAL FOREIGN KEY (travel_journal_id) REFERENCES travel_journal (id);

ALTER TABLE report_travel_journal
    ADD CONSTRAINT FK_REPORT_TRAVEL_JOURNAL_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX report_travel_journal_foreign_index ON REPORT_TRAVEL_JOURNAL (travel_journal_id);
CREATE INDEX report_travel_journal_foreign_index_2 ON REPORT_TRAVEL_JOURNAL (user_id);
