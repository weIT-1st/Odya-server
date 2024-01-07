CREATE TABLE representative_travel_journal
(
    id                NUMERIC(19, 0) NOT NULL,
    travel_journal_id NUMERIC(19, 0) NOT NULL,
    user_id           NUMERIC(19, 0) NOT NULL,
    created_date      DATE           NOT NULL,
    CONSTRAINT pk_representative_travel_journal PRIMARY KEY (id)
);

CREATE SEQUENCE representative_travel_journal_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX representative_travel_journal_travel_journal_id_index ON representative_travel_journal (travel_journal_id);
ALTER TABLE representative_travel_journal
    ADD CONSTRAINT representative_travel_journal_unique UNIQUE (user_id, travel_journal_id);
