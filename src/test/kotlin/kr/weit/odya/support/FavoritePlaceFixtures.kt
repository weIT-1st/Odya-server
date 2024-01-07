package kr.weit.odya.support

import kr.weit.odya.domain.favoritePlace.FavoritePlace
import kr.weit.odya.domain.favoritePlace.FavoritePlaceSortType
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.FavoritePlaceRequest
import kr.weit.odya.service.dto.FavoritePlaceResponse
import kr.weit.odya.service.dto.SliceFavoritePlaceResponse

const val TEST_FAVORITE_PLACE_ID = 1L
const val TEST_OTHER_FAVORITE_PLACE_ID = 2L
const val TEST_INVALID_FAVORITE_PLACE_ID = -1L
const val TEST_EXIST_FAVORITE_PLACE_ID = 10L
const val TEST_FAVORITE_PLACE_COUNT = 1
val TEST_FAVORITE_PLACE_SORT_TYPE: FavoritePlaceSortType = FavoritePlaceSortType.LATEST
const val TEST_FAVORITE_PLACE_INVALID_SORT_TYPE = "OLDEST"

fun createFavoritePlace(user: User = createUser()): FavoritePlace {
    return FavoritePlace(TEST_FAVORITE_PLACE_ID, TEST_PLACE_ID, user)
}

fun createOtherFavoritePlace(user: User = createUser()): FavoritePlace {
    return FavoritePlace(TEST_OTHER_FAVORITE_PLACE_ID, TEST_OTHER_PLACE_ID, user)
}

fun createFavoritePlaceRequest(): FavoritePlaceRequest {
    return FavoritePlaceRequest(TEST_PLACE_ID)
}

fun createFavoritePlaceList(): List<FavoritePlace> {
    return listOf(createFavoritePlace())
}

fun createSliceFavoritePlaceResponse(): SliceFavoritePlaceResponse {
    return SliceFavoritePlaceResponse(TEST_DEFAULT_SIZE, createFavoritePlaceList().map { FavoritePlaceResponse(it, true) })
}
