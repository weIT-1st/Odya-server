package kr.weit.odya.domain.favoritePlace

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun FavoritePlaceRepository.getByFavoritePlaceId(favoritePlaceId: Long): FavoritePlace =
    findByIdOrNull(favoritePlaceId) ?: throw NoSuchElementException("해당 장소는 관심 장소 등록되어있지 않습니다.")

@Repository
interface FavoritePlaceRepository : JpaRepository<FavoritePlace, Long> {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean
}
