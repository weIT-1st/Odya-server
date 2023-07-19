package kr.weit.odya.service.dto

import kr.weit.odya.domain.interestPlace.InterestPlace
import kr.weit.odya.domain.user.User

class InterestPlaceRequest(
    val placeId: String,
) {
    fun toEntity(user: User): InterestPlace = InterestPlace(0L, placeId, user)
}
