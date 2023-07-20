package kr.weit.odya.support

import kr.weit.odya.domain.interestPlace.InterestPlace
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.InterestPlaceRequest

const val TEST_INTEREST_PLACE_ID = 1L
const val TEST_INVALID_INTEREST_PLACE_ID = -1L
const val TEST_EXIST_INTEREST_PLACE_ID = 10L
fun createInterestPlace(user: User = createUser()) = InterestPlace(TEST_INTEREST_PLACE_ID, TEST_PLACE_ID, user)

fun createInterestPlaceRequest() = InterestPlaceRequest(TEST_PLACE_ID)
