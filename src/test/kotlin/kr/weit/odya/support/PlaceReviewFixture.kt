package kr.weit.odya.support

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewSortType
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import kr.weit.odya.service.dto.SlicePlaceReviewResponse

const val TEST_PLACE_REVIEW_ID = 1L
const val TEST_OTHER_PLACE_REVIEW_ID_2 = 2L
const val TEST_OTHER_PLACE_REVIEW_ID_3 = 3L
const val TEST_INVALID_PLACE_REVIEW_ID = -1L
const val TEST_EXIST_PLACE_REVIEW_ID = 5L
const val TEST_PLACE_ID = "test_place_id"
const val TEST_OTHER_PLACE_ID = "test_other_place_id"
const val TEST_REVIEW = "test review"
const val TEST_RATING = 5
const val TEST_UPDATE_REVIEW = "test update review"
const val TEST_UPDATE_RATING = 4
const val TEST_TOO_LOW_RATING = 0
const val TEST_TOO_HIGH_RATING = 11
const val TEST_TOO_LONG_REVIEW = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit"
const val TEST_LAST_ID = 1L
const val TEST_INVALID_LAST_ID = -1L
val TEST_PLACE_SORT_TYPE: PlaceReviewSortType = PlaceReviewSortType.OLDEST
fun createPlaceReviewRequest(): PlaceReviewCreateRequest {
    return PlaceReviewCreateRequest(TEST_PLACE_ID, TEST_RATING, TEST_REVIEW)
}

fun updatePlaceReviewRequest(): PlaceReviewUpdateRequest {
    return PlaceReviewUpdateRequest(TEST_PLACE_REVIEW_ID, TEST_UPDATE_RATING, TEST_UPDATE_REVIEW)
}

fun createPlaceReview(user: User = createUser()): PlaceReview {
    return PlaceReview(TEST_PLACE_REVIEW_ID, TEST_PLACE_ID, user, TEST_RATING, TEST_REVIEW)
}

fun createOtherPlaceReview(user: User, placeReviewId: Long = TEST_OTHER_PLACE_REVIEW_ID_2, placeId: String = TEST_PLACE_ID): PlaceReview {
    return PlaceReview(placeReviewId, placeId, user, TEST_RATING, TEST_REVIEW)
}

fun creatSlicePlaceReviewResponse(): SlicePlaceReviewResponse {
    return SlicePlaceReviewResponse.of(TEST_SIZE, listOf(createPlaceReview(createUser())))
}
