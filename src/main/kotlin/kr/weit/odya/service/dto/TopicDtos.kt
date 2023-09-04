package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotEmpty
import kr.weit.odya.domain.favoriteTopic.FavoriteTopic

data class AddFavoriteTopicRequest(
    @field:NotEmpty(message = "관심 토픽 ID는 필수 입력값입니다.")
    val topicIdList: List<Long>,
)

data class FavoriteTopicListResponse(val id: Long, val userId: Long, val topicId: Long, val topicWord: String) {
    constructor(favoriteTopic: FavoriteTopic) : this(
        favoriteTopic.id,
        favoriteTopic.registrantsId,
        favoriteTopic.topic.id,
        favoriteTopic.topic.word,
    )
}

data class TopicResponse(
    val id: Long,
    val topic: String,
)
