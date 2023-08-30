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


