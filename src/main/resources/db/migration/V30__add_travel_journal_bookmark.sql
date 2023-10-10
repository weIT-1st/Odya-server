CREATE TABLE travel_journal_bookmark
(
    id                NUMERIC(19, 0) NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
    created_date      DATE           NOT NULL,
    CONSTRAINT pk_travel_journal_bookmark PRIMARY KEY (id)
);

CREATE SEQUENCE travel_journal_bookmark_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX travel_journal_bookmark_travel_journal_id_index ON travel_journal_bookmark (travel_journal_id);
ALTER TABLE travel_journal_bookmark
    ADD CONSTRAINT travel_journal_bookmark_unique UNIQUE (user_id, travel_journal_id);
