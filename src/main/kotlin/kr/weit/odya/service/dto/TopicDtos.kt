package kr.weit.odya.service.dto

import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.topic.Topic

data class FavoriteTopicListResponse(val id: Long, val userId: Long, val topic: Topic) {
    constructor(favoriteTopic: FavoriteTopic) : this(
        favoriteTopic.id,
        favoriteTopic.registrantsId,
        favoriteTopic.topic,
    )
}
