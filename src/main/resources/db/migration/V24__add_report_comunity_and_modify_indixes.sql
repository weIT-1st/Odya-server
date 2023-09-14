rename PLACE_REVIEW_FOREIGN_INDEX to PLACE_REVIEW_USER_ID_INDEX;

rename REPORT_PLACE_REVIEW_FOREIGN_INDEX to REPORT_PLACE_REVIEW_PLACE_REVIEW_ID_INDEX;

rename REPORT_PLACE_REVIEW_FOREIGN_INDEX_2 to REPORT_PLACE_REVIEW_USER_ID_INDEX;

rename REPORT_TRAVEL_JOURNAL_FOREIGN_INDEX to REPORT_TRAVEL_JOURNAL_TRAVEL_JOURNAL_ID_INDEX;

rename REPORT_TRAVEL_JOURNAL_FOREIGN_INDEX_2 to REPORT_TRAVEL_JOURNAL_USER_ID_INDEX;

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

CREATE INDEX report_community_community_id_index ON report_community (community_id);

ALTER TABLE report_community
    ADD CONSTRAINT FK_REPORT_COMMUNITY_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX report_community_user_id_index ON report_community (user_id);
