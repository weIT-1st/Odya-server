package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.favoriteTopic.FavoriteTopicRepository
import kr.weit.odya.domain.favoriteTopic.getByFavoriteTopicId
import kr.weit.odya.domain.favoriteTopic.getByUserId
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
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
    fun addFavoriteTopic(userId: Long, topicIdList: List<Long>) {
        val user = userRepository.getByUserId(userId)
        topicIdList.forEach {
            if (!favoriteTopicRepository.existsByUserAndTopicId(user, it)) {
                favoriteTopicRepository.save(
                    FavoriteTopic(0L, user, topicRepository.getByTopicId(it)),
                )
            }
        }
    }

    @Transactional
    fun deleteFavoriteTopic(userId: Long, favoriteTopicId: Long) {
        val favoriteTopic = favoriteTopicRepository.getByFavoriteTopicId(favoriteTopicId).also { favoriteTopic ->
            require(favoriteTopic.registrantsId != userId) { throw ForbiddenException("관심 토픽을 삭제할 권한이 없습니다.") }
        }
        favoriteTopicRepository.delete(favoriteTopic)
    }

    fun getFavoriteTopicList(userId: Long): List<Topic> {
        return favoriteTopicRepository.getByUserId(userId).map { it.topic }
    }
}
