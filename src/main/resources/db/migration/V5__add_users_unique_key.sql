ALTER TABLE users
    ADD CONSTRAINT UK_EMAIL_ON_USERS UNIQUE (email);
ALTER TABLE users
    ADD CONSTRAINT UK_PHONE_NUMBER_ON_USERS UNIQUE (phone_number);