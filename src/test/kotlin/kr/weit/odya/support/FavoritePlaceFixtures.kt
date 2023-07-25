package kr.weit.odya.support

import kr.weit.odya.domain.favoritePlace.FavoritePlace
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.FavoritePlaceRequest

const val TEST_FAVORITE_PLACE_ID = 1L
const val TEST_INVALID_FAVORITE_PLACE_ID = -1L
const val TEST_EXIST_FAVORITE_PLACE_ID = 10L
fun createFavoritePlace(user: User = createUser()): FavoritePlace {
    return FavoritePlace(TEST_FAVORITE_PLACE_ID, TEST_PLACE_ID, user)
}

fun createFavoritePlaceRequest(): FavoritePlaceRequest {
    return FavoritePlaceRequest(TEST_PLACE_ID)
}
