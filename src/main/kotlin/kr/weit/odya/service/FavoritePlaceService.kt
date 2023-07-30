package kr.weit.odya.service

import jakarta.transaction.Transactional
import kr.weit.odya.domain.favoritePlace.FavoritePlaceRepository
import kr.weit.odya.domain.favoritePlace.FavoritePlaceSortType
import kr.weit.odya.domain.favoritePlace.getByFavoritePlaceId
import kr.weit.odya.domain.favoritePlace.getByFavoritePlaceList
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.FavoritePlaceRequest
import kr.weit.odya.service.dto.FavoritePlaceResponse
import kr.weit.odya.service.dto.SliceFavoritePlaceResponse
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

    fun getFavoritePlaceCount(userId: Long): Int {
        return favoritePlaceRepository.countByUserId(userId)
    }

    @Transactional
    fun getFavoritePlaceList(userId: Long, size: Int, sortType: FavoritePlaceSortType, lastId: Long?): SliceFavoritePlaceResponse {
        val user = userRepository.getByUserId(userId)
        return SliceFavoritePlaceResponse(size, favoritePlaceRepository.getByFavoritePlaceList(user, size, sortType, lastId).map { FavoritePlaceResponse(it) })
    }
}
