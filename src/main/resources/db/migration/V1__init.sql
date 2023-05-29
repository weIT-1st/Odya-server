create table users
(
    id            number(19, 0) not null,
    username      varchar(50)   not null,
    email         varchar(255)  not null,
    nickname      varchar(20)   not null,
    phone_number  varchar(13)   not null,
    gender        varchar(1)    not null,
    birthday      date          not null,
    profile_name  varchar(255)  not null,
    social_type   varchar(10)   not null,
    withdraw      number(1, 0)  not null,
    withdraw_date date          null,
    created_date  date          not null,
    primary key (id)
);

create sequence users_seq start with 1 increment by 1;
