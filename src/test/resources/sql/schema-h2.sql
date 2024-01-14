CREATE ALIAS IF NOT EXISTS H2GIS_SPATIAL FOR "org.h2gis.functions.factory.H2GISFunctions.load";
CALL H2GIS_SPATIAL();

DROP ALIAS ST_WITHIN;
CREATE ALIAS ST_WITHIN FOR "kr.weit.odya.support.SQLSupport.within";
CREATE ALIAS rectangle FOR "org.h2gis.functions.spatial.create.ST_MakeEnvelope.makeEnvelope";

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

CREATE INDEX place_review_user_id_index ON place_review (user_id);

CREATE TABLE follow
(
    follower_id  NUMERIC(19, 0) NOT NULL,
    following_id NUMERIC(19, 0) NOT NULL,
    created_date DATE           NOT NULL,
    CONSTRAINT pk_follow PRIMARY KEY (follower_id, following_id)
);

-- follower_id에는 이미 index가 걸려있어서 생략
CREATE INDEX follow_following_id_index ON follow (following_id);

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

CREATE INDEX profile_profile_color_id_index on PROFILE (profile_color_id);

alter table users
    add constraint FK_USERS_ON_PROFILE foreign key (profile_id) references profile (id);

CREATE INDEX users_profile_id_index ON users (profile_id);

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

CREATE INDEX favorite_place_user_id_index ON FAVORITE_PLACE (user_id);

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
    place_id     VARCHAR2(400)  NULL,
    coordinate   GEOMETRY       NULL,
    place_name   VARCHAR2(90)   NULL,
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

CREATE INDEX content_image_place_id_index ON CONTENT_IMAGE (place_id);

CREATE INDEX content_image_coordinate_index ON CONTENT_IMAGE (coordinate);

CREATE SEQUENCE travel_journal_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE travel_journal_content_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE travel_companion_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE content_image_seq START WITH 1 INCREMENT BY 1;

CREATE SEQUENCE travel_journal_content_image_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX travel_journal_content_travel_journal_place_id_index ON TRAVEL_JOURNAL_CONTENT (place_id);

alter table travel_journal
    add constraint FK_TRAVEL_JOURNAL_ON_USERS foreign key (user_id) references users (id);

CREATE INDEX travel_journal_travel_journal_user_id_index ON TRAVEL_JOURNAL (user_id);

alter table travel_journal_content_image
    add constraint FK_TRAVEL_IMAGE_ON_TRAVEL_JOURNAL_CONTENT foreign key (travel_journal_content_id) references travel_journal_content (id);

CREATE INDEX travel_journal_content_image_travel_journal_content_id_index ON TRAVEL_JOURNAL_CONTENT_IMAGE (travel_journal_content_id);

alter table travel_journal_content_image
    add constraint FK_TRAVEL_IMAGE_ON_CONTENT_IMAGE foreign key (content_image_id) references content_image (id);

CREATE INDEX travel_journal_content_image_content_image_id_index ON TRAVEL_JOURNAL_CONTENT_IMAGE (content_image_id);

alter table content_image
    add constraint FK_CONTENT_IMAGE_ON_USERS foreign key (user_id) references users (id);

CREATE INDEX content_image_user_id_index ON CONTENT_IMAGE (user_id);

alter table travel_journal_content
    add constraint FK_TRAVEL_JOURNAL_CONTENT_ON_TRAVEL_JOURNAL foreign key (travel_journal_id) references travel_journal (id);

CREATE INDEX travel_journal_content_travel_journal_id_index ON TRAVEL_JOURNAL_CONTENT (travel_journal_id);

alter table travel_companion
    add constraint FK_TRAVEL_COMPANION_ON_TRAVEL_JOURNAL foreign key (travel_journal_id) references travel_journal (id);

CREATE INDEX travel_companion_travel_journal_id_index ON TRAVEL_COMPANION (travel_journal_id);

alter table travel_companion
    add constraint FK_TRAVEL_COMPANION_ON_USERS foreign key (user_id) references users (id);

CREATE INDEX travel_companion_user_id_index ON TRAVEL_COMPANION (user_id);

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

CREATE INDEX favorite_topic_topic_id_index ON FAVORITE_TOPIC (topic_id);

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

CREATE INDEX agreed_terms_terms_id_index ON AGREED_TERMS (terms_id);

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

CREATE INDEX community_place_id_index ON COMMUNITY (place_id);

alter table community
    add constraint FK_COMMUNITY_ON_TOPIC foreign key (topic_id) references topic (id);

CREATE INDEX community_topic_id_index ON COMMUNITY (topic_id);

alter table community
    add constraint FK_COMMUNITY_ON_TRAVEL_JOURNAL foreign key (travel_journal_id) references travel_journal (id);

CREATE INDEX community_travel_journal_id_index ON COMMUNITY (travel_journal_id);

alter table community
    add constraint FK_COMMUNITY_ON_USER foreign key (user_id) references users (id);

CREATE INDEX community_user_id_index ON COMMUNITY (user_id);

alter table community_content_image
    add constraint FK_COMMUNITY_CONTENT_IMAGE_ON_COMMUNITY foreign key (community_id) references community (id);

CREATE INDEX community_content_image_community_id_index ON COMMUNITY_CONTENT_IMAGE (community_id);

alter table community_content_image
    add constraint FK_COMMUNITY_CONTENT_IMAGE_ON_CONTENT_IMAGE foreign key (content_image_id) references content_image (id);

CREATE INDEX community_content_image_content_image_id_index ON COMMUNITY_CONTENT_IMAGE (content_image_id);

CREATE TABLE community_comment
(
    id           NUMBER(19, 0) NOT NULL,
    content      VARCHAR2(300) NOT NULL,
    created_date TIMESTAMP     NOT NULL,
    updated_date TIMESTAMP     NOT NULL,
    user_id      NUMBER(19, 0) NOT NULL,
    community_id NUMBER(19, 0) NOT NULL,
    CONSTRAINT pk_community_comment PRIMARY KEY (id)
);

CREATE SEQUENCE community_comment_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE community_comment
    ADD CONSTRAINT FK_COMMUNITY_COMMENT_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX community_comment_user_id_index ON COMMUNITY_COMMENT (user_id);

ALTER TABLE community_comment
    ADD CONSTRAINT FK_COMMUNITY_COMMENT_ON_COMMUNITY FOREIGN KEY (community_id) REFERENCES community (id);

CREATE INDEX community_index ON community_comment (community_id);

CREATE SEQUENCE report_place_review_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_place_review
(
    id              NUMBER(19, 0) NOT NULL,
    created_date    TIMESTAMP     NOT NULL,
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

CREATE INDEX report_place_review_place_review_id_index ON report_place_review (place_review_id);

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

CREATE INDEX report_travel_journal_user_id_index ON report_travel_journal (user_id);

CREATE SEQUENCE report_community_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_community
(
    id            NUMBER(19, 0) NOT NULL,
    created_date  TIMESTAMP     NOT NULL,
    community_id  NUMBER(19, 0) NOT NULL,
    user_id       NUMBER(19, 0) NOT NULL,
    report_reason VARCHAR2(20)  NOT NULL,
    other_reason  VARCHAR2(60),
    CONSTRAINT pk_report_community PRIMARY KEY (id)
);

ALTER TABLE report_community
    ADD CONSTRAINT report_community_unique UNIQUE (community_id, user_id);

ALTER TABLE report_community
    ADD CONSTRAINT FK_REPORT_COMMUNITY_ON_COMMUNITY FOREIGN KEY (community_id) REFERENCES community (id);

ALTER TABLE report_community
    ADD CONSTRAINT FK_REPORT_COMMUNITY_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX report_community_user_id_index ON report_community (user_id);

CREATE TABLE community_like
(
    community_id NUMERIC(19, 0) NOT NULL,
    user_id      NUMERIC(19, 0) NOT NULL,
    created_date DATE           NOT NULL,
    CONSTRAINT pk_community_like PRIMARY KEY (community_id, user_id)
);

CREATE INDEX community_like_create_date_index ON community_like (created_date);

ALTER TABLE community
    ADD like_count INTEGER DEFAULT 0 NOT NULL;

CREATE INDEX community_like_count_index ON community (like_count);

CREATE TABLE travel_journal_bookmark
(
    id                NUMERIC(19, 0) NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
    created_date      DATE           NOT NULL,
    CONSTRAINT pk_travel_journal_bookmark PRIMARY KEY (id)
);

CREATE SEQUENCE travel_journal_bookmark_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX travel_journal_bookmark_travel_journal_id_index ON travel_journal_bookmark (travel_journal_id);
ALTER TABLE travel_journal_bookmark
    ADD CONSTRAINT travel_journal_bookmark_unique UNIQUE (user_id, travel_journal_id);

CREATE TABLE representative_travel_journal
(
    id                NUMERIC(19, 0) NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
    created_date      DATE           NOT NULL,
    CONSTRAINT pk_representative_travel_journal PRIMARY KEY (id)
);

CREATE SEQUENCE representative_travel_journal_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX representative_travel_journal_travel_journal_id_index ON representative_travel_journal (travel_journal_id);
ALTER TABLE representative_travel_journal
    ADD CONSTRAINT representative_travel_journal_unique UNIQUE (user_id, travel_journal_id);

CREATE INDEX travel_journal_content_travel_travel_date_index ON travel_journal_content (travel_date);
