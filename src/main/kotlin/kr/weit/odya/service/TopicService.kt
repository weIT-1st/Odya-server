package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.favoriteTopic.FavoriteTopicRepository
import kr.weit.odya.domain.favoriteTopic.getByFavoriteTopicId
import kr.weit.odya.domain.favoriteTopic.getByUserId
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.topic.getPopularTopicsAtPlace
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.AddFavoriteTopicRequest
import kr.weit.odya.service.dto.FavoriteTopicListResponse
import kr.weit.odya.service.dto.TopicResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TopicService(
    private val topicRepository: TopicRepository,
    private val favoriteTopicRepository: FavoriteTopicRepository,
    private val userRepository: UserRepository,
) {
    fun getTopicList(): List<Topic> {
        return topicRepository.findAll()
    }

    @Transactional
    fun addFavoriteTopic(userId: Long, request: AddFavoriteTopicRequest) {
        val user = userRepository.getByUserId(userId)
        val topicList = request.topicIdList.mapNotNull {
            if (!favoriteTopicRepository.existsByUserAndTopicId(user, it)) {
                FavoriteTopic(0L, user, topicRepository.getByTopicId(it))
            } else {
                null
            }
        }
        favoriteTopicRepository.saveAll(topicList)
    }

    @Transactional
    fun deleteFavoriteTopic(userId: Long, favoriteTopicId: Long) {
        favoriteTopicRepository.delete(
            favoriteTopicRepository.getByFavoriteTopicId(favoriteTopicId).also {
                require(it.registrantsId == userId) { throw ForbiddenException("관심 토픽을 삭제할 권한이 없습니다.") }
            },
        )
    }

    fun getFavoriteTopicList(userId: Long): List<FavoriteTopicListResponse> {
        return favoriteTopicRepository.getByUserId(userId).map { FavoriteTopicListResponse(it) }
    }

    fun getPopularTopicsAtPlace(placeId: String, size: Int): List<TopicResponse> {
        return topicRepository.getPopularTopicsAtPlace(placeId, size).map { TopicResponse(it.id, it.word) }
    }
}
