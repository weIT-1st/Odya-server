CREATE INDEX users_profile_id_index ON USERS (profile_id);

CREATE INDEX follow_following_id_index ON FOLLOW (following_id);
-- follower_id에는 이미 index가 걸려있어서 생략

CREATE INDEX profile_profile_color_id_index ON PROFILE (profile_color_id);

CREATE INDEX favorite_place_user_id_index ON FAVORITE_PLACE (user_id);

CREATE INDEX travel_journal_user_id_index ON TRAVEL_JOURNAL (user_id);

CREATE INDEX travel_journal_content_image_travel_journal_content_id_index ON TRAVEL_JOURNAL_CONTENT_IMAGE (travel_journal_content_id);

CREATE INDEX travel_journal_content_image_content_image_id_index ON TRAVEL_JOURNAL_CONTENT_IMAGE (content_image_id);

CREATE INDEX content_image_user_id_index ON CONTENT_IMAGE (user_id);

CREATE INDEX travel_journal_content_travel_journal_id_index ON TRAVEL_JOURNAL_CONTENT (travel_journal_id);

CREATE INDEX travel_journal_content_place_id_index ON TRAVEL_JOURNAL_CONTENT (place_id);

CREATE INDEX travel_companion_travel_journal_id_index ON TRAVEL_COMPANION (travel_journal_id);

CREATE INDEX travel_companion_user_id_index ON TRAVEL_COMPANION (user_id);

CREATE INDEX favorite_topic_topic_id_index ON FAVORITE_TOPIC (topic_id);

CREATE INDEX agreed_terms_terms_id_index ON AGREED_TERMS (terms_id);

CREATE INDEX community_topic_id_index ON COMMUNITY (topic_id);

CREATE INDEX community_travel_journal_id_index ON COMMUNITY (travel_journal_id);

CREATE INDEX community_user_id_index ON COMMUNITY (user_id);

CREATE INDEX community_place_id_index ON COMMUNITY (place_id);

CREATE INDEX community_content_image_community_id_index ON COMMUNITY_CONTENT_IMAGE (community_id);

CREATE INDEX community_content_image_content_image_id_index ON COMMUNITY_CONTENT_IMAGE (content_image_id);

CREATE INDEX community_content_user_id_index ON COMMUNITY_COMMENT (user_id);
