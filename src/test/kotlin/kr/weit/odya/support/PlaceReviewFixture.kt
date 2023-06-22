package kr.weit.odya.support

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ConversionPlaceReview
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

const val TEST_PLACE_REVIEW_ID = 1L
const val TEST_INVALID_PLACE_REVIEW_ID = -1L
const val TEST_EXIST_PLACE_REVIEW_ID = 2L
const val TEST_PLACE_ID = "test_place_id"
const val TEST_REVIEW = "test review"
const val TEST_RATING = 5
const val TEST_UPDATE_REVIEW = "test update review"
const val TEST_UPDATE_RATING = 4
const val TEST_TOO_LOW_RATING = 0
const val TEST_TOO_HIGH_RATING = 11
const val TEST_TOO_LONG_REVIEW = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit"
const val TEST_START_ID = 10L
const val TEST_INVALID_START_ID = -1L
const val TEST_COUNT = 10
const val TEST_INVALID_COUNT = -1

fun createPlaceReviewRequest(): PlaceReviewCreateRequest {
    return PlaceReviewCreateRequest(TEST_PLACE_ID, TEST_RATING, TEST_REVIEW)
}

fun updatePlaceReviewRequest(): PlaceReviewUpdateRequest {
    return PlaceReviewUpdateRequest(TEST_PLACE_REVIEW_ID, TEST_UPDATE_RATING, TEST_UPDATE_REVIEW)
}

fun createPlaceReview(user: User = createUser()): PlaceReview {
    return PlaceReview(TEST_PLACE_REVIEW_ID, TEST_PLACE_ID, user, TEST_RATING, TEST_REVIEW)
}

fun conversionPlaceReview(): ConversionPlaceReview {
    return ConversionPlaceReview(TEST_PLACE_REVIEW_ID, TEST_PLACE_ID, TEST_USER_ID, TEST_NICKNAME, TEST_RATING, TEST_REVIEW)
}

fun creatPlaceReviewListResponse(): PlaceReviewListResponse {
    return PlaceReviewListResponse(listOf(conversionPlaceReview()), TEST_PLACE_REVIEW_ID, false)
}

fun createPlaceReviewPage(): PageRequest {
    return PageRequest.of(0, TEST_COUNT)
}

fun createSlicePlaceReview(): Slice<PlaceReview> {
    return SliceImpl(listOf(createPlaceReview()), createPlaceReviewPage(), true)
}
