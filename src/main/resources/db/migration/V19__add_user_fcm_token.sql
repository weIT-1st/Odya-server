ALTER TABLE USERS
    ADD FCM_TOKEN VARCHAR(255) NULL;
ALTER TABLE users
    ADD CONSTRAINT UK_FCM_TOKEN_ON_USERS UNIQUE (fcm_token);