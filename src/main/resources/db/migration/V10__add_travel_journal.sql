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
    content           VARCHAR2(600)  NOT NULL,
    place_id          VARCHAR2(400)  NOT NULL,
    coordinates       CLOB           NOT NULL,
    travel_date       DATE           NOT NULL,
    created_date      TIMESTAMP      NOT NULL,
    updated_date      TIMESTAMP      NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    CONSTRAINT pk_travel_journal_content PRIMARY KEY (id)
);

CREATE TABLE travel_companion
(
    id                NUMERIC(19, 0) NOT NULL,
    created_date      TIMESTAMP      NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
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
