CREATE SEQUENCE report_place_review_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_place_review
(
    id              NUMBER(19, 0) NOT NULL,
    created_date    TIMESTAMP     NOT NULL,
    user_id         NUMBER(19, 0) NOT NULL,
    place_review_id NUMBER(19, 0) NOT NULL,
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

CREATE SEQUENCE report_travel_journal_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_travel_journal
(
    id                NUMBER(19, 0) NOT NULL,
    created_date      TIMESTAMP     NOT NULL,
    user_id           NUMBER(19, 0) NOT NULL,
    travel_journal_id NUMBER(19, 0) NOT NULL,
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
