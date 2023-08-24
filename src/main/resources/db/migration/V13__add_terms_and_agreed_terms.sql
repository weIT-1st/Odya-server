CREATE SEQUENCE terms_topic_seq START WITH 1 INCREMENT BY 1;

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

CREATE SEQUENCE agreed_terms_topic_seq START WITH 1 INCREMENT BY 1;

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
