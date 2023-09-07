CREATE INDEX place_review_foreign_index ON place_review (user_id);

CREATE INDEX report_place_review_foreign_index ON REPORT_PLACE_REVIEW (place_review_id);
CREATE INDEX report_place_review_foreign_index_2 ON REPORT_PLACE_REVIEW (user_id);

CREATE INDEX report_travel_journal_foreign_index ON REPORT_TRAVEL_JOURNAL (travel_journal_id);
CREATE INDEX report_travel_journal_foreign_index_2 ON REPORT_TRAVEL_JOURNAL (user_id);
