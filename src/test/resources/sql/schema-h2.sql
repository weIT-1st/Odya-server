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
