package kr.weit.odya.support

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewSortType
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ExistReviewResponse
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import kr.weit.odya.service.dto.ReviewCountResponse
import kr.weit.odya.service.dto.SlicePlaceReviewResponse
import java.time.LocalDateTime

const val TEST_PLACE_REVIEW_ID = 1L
const val TEST_INVALID_PLACE_REVIEW_ID = -1L
const val TEST_NOT_EXIST_PLACE_REVIEW_ID = 5L
const val TEST_PLACE_ID = "test_place_id"
const val TEST_OTHER_PLACE_ID = "test_other_place_id"
const val TEST_UPDATE_PLACE_ID = "test_update_place_id"
const val TEST_REVIEW = "test review"
const val TEST_HIGHEST_RATING = 10
const val TEST_LOWEST_RATING = 1
const val TEST_UPDATE_REVIEW = "test update review"
const val TEST_UPDATE_RATING = 4
const val TEST_TOO_LOW_RATING = 0
const val TEST_TOO_HIGH_RATING = 11
const val TEST_TOO_LONG_PHRASE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
const val TEST_LAST_ID = 3L
const val TEST_INVALID_LAST_ID = -1L
val TEST_PLACE_SORT_TYPE: PlaceReviewSortType = PlaceReviewSortType.LATEST
const val TEST_AVERAGE_RATING = 5.5
const val TEST_PLACE_REVIEW_COUNT = 2

class MockPlaceReview(
    id: Long,
    placeId: String,
    user: User,
    starRating: Int,
    review: String,
) : PlaceReview(id, placeId, user, starRating, review) { // createDate가 null이면 테스트에서 오류가 발생하므로 Mock엔티티 생성
    override var createdDate: LocalDateTime = LocalDateTime.of(2023, 9, 1, 0, 0, 0)
}

fun createPlaceReviewRequest(): PlaceReviewCreateRequest {
    return PlaceReviewCreateRequest(TEST_PLACE_ID, TEST_HIGHEST_RATING, TEST_REVIEW)
}

fun updatePlaceReviewRequest(): PlaceReviewUpdateRequest {
    return PlaceReviewUpdateRequest(TEST_PLACE_REVIEW_ID, TEST_UPDATE_RATING, TEST_UPDATE_REVIEW)
}

fun createPlaceReview(user: User = createUser()): PlaceReview {
    return PlaceReview(0L, TEST_PLACE_ID, user, TEST_HIGHEST_RATING, TEST_REVIEW)
}

fun createMockPlaceReview(user: User = createUser()): MockPlaceReview {
    return MockPlaceReview(TEST_PLACE_REVIEW_ID, TEST_PLACE_ID, user, TEST_HIGHEST_RATING, TEST_REVIEW)
}

fun createLowestRatingPlaceReview(user: User = createUser()): PlaceReview {
    return PlaceReview(0L, TEST_PLACE_ID, user, TEST_LOWEST_RATING, TEST_REVIEW)
}

fun createLatestReview(user: User = createUser()): PlaceReview {
    return PlaceReview(0L, TEST_OTHER_PLACE_ID, user, TEST_LOWEST_RATING, TEST_REVIEW)
}

fun creatSlicePlaceReviewResponse(): SlicePlaceReviewResponse {
    return SlicePlaceReviewResponse.of(
        TEST_SIZE,
        listOf(createMockPlaceReview(createUser())).map {
            PlaceReviewListResponse(
                it,
                TEST_PROFILE_URL,
            )
        },
        TEST_AVERAGE_RATING,
    )
}

fun createExistReviewResponse(exist: Boolean = true) = ExistReviewResponse(exist)

fun createCountPlaceReviewResponse(count: Int = TEST_PLACE_REVIEW_COUNT) = ReviewCountResponse(count)
