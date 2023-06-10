package kr.weit.odya.support

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest

const val TEST_PLACE_REVIEW_ID = 1L
const val TEST_INVALID_PLACE_REVIEW_ID = -1L
const val TEST_EXIST_PLACE_REVIEW_ID = 2L
const val TEST_PLACE_ID = "test_place_id"
const val TEST_REVIEW = "test review"
const val TEST_RATING = 5
const val TEST_UPDATE_REVIEW = "test update review"
const val TEST_UPDATE_RATING = 4
const val TEST_TOO_LOW_RATING = -1
const val TEST_TOO_HIGH_RATING = 11
const val TEST_TOO_LONG_REVIEW = "김수한무 거북이와 두루미 삼천갑자 동방삭 치치카포 사리사리센타 워리워리 세브리깡 무두셀라 구름이 허리케인에 담벼락 담벼락에 서생원 서생원에 고양이 고양이엔 바둑이 바둑이는 돌돌이"


fun createPlaceReviewRequest(): PlaceReviewCreateRequest {
    return PlaceReviewCreateRequest(TEST_PLACE_ID, TEST_RATING, TEST_REVIEW)
}

fun updatePlaceReviewRequest(): PlaceReviewUpdateRequest {
    return PlaceReviewUpdateRequest(TEST_PLACE_REVIEW_ID, TEST_UPDATE_RATING, TEST_UPDATE_REVIEW)
}

fun createPlaceReview(user: User = createUser()): PlaceReview {
    return PlaceReview(TEST_PLACE_REVIEW_ID, TEST_PLACE_ID, user, TEST_RATING, TEST_REVIEW)
}
