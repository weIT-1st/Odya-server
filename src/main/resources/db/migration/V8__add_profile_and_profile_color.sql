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
    updated_date DATE           NOT NULL,
    CONSTRAINT pk_profile_color PRIMARY KEY (id)
);

create sequence profile_color_seq start with 1 increment by 1;

alter table users
    drop column profile_name;

alter table users
    add profile_id numeric(19, 0) not null;

alter table profile
    add constraint FK_PROFILE_ON_PROFILE_COLOR foreign key (profile_color_id) references profile_color (id);

alter table users
    add constraint FK_USERS_ON_PROFILE foreign key (profile_id) references profile (id);
