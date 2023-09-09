package kr.weit.odya.domain.favoriteTopic

import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun FavoriteTopicRepository.getByFavoriteTopicId(favoriteTopicId: Long): FavoriteTopic =
    findByIdOrNull(favoriteTopicId) ?: throw NoSuchElementException("삭제할 관심 토픽이 등록되어있지 않습니다.")

fun FavoriteTopicRepository.getByUserId(userId: Long): List<FavoriteTopic> =
    findByUserId(userId)

@Repository
interface FavoriteTopicRepository : JpaRepository<FavoriteTopic, Long> {
    fun existsByUserAndTopicId(user: User, topicId: Long): Boolean

    fun findByUserId(userId: Long): List<FavoriteTopic>

    fun deleteAllByUserId(userId: Long)
}
