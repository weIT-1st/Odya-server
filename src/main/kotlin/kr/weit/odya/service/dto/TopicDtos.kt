package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotEmpty
import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.topic.Topic

data class AddFavoriteTopicRequest(
    @field:NotEmpty(message = "관심 토픽 ID는 필수 입력값입니다.")
    val topicIdList: List<Long>,
)

data class FavoriteTopicListResponse(val id: Long, val userId: Long, val topic: Topic) {
    constructor(favoriteTopic: FavoriteTopic) : this(
        favoriteTopic.id,
        favoriteTopic.registrantsId,
        favoriteTopic.topic,
    )
}
