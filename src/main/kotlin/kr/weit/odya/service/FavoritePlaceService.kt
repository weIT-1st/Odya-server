package kr.weit.odya.service

import jakarta.transaction.Transactional
import kr.weit.odya.domain.favoritePlace.FavoritePlaceRepository
import kr.weit.odya.domain.favoritePlace.getByFavoritePlaceId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.FavoritePlaceRequest
import org.springframework.stereotype.Service

@Service
class FavoritePlaceService(private val favoritePlaceRepository: FavoritePlaceRepository, private val userRepository: UserRepository) {
    @Transactional
    fun createFavoritePlace(userId: Long, request: FavoritePlaceRequest) {
        if (favoritePlaceRepository.existsByUserIdAndPlaceId(userId, request.placeId)) {
            throw ExistResourceException("${request.placeId}: 해당 장소는 이미 관심 장소입니다")
        }
        favoritePlaceRepository.save(request.toEntity(userRepository.getByUserId(userId)))
    }

    @Transactional
    fun deleteFavoritePlace(userId: Long, favoritePlaceId: Long) {
        favoritePlaceRepository.delete(favoritePlaceRepository.getByFavoritePlaceId(favoritePlaceId))
    }

    fun getFavoritePlace(userId: Long, placeId: String): Boolean {
        return favoritePlaceRepository.existsByUserIdAndPlaceId(userId, placeId)
    }
}
