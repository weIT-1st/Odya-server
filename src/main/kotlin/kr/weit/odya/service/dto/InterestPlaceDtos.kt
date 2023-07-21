package kr.weit.odya.service.dto

import kr.weit.odya.domain.interestPlace.InterestPlace
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NullOrNotBlank

data class InterestPlaceRequest(
    @field:NullOrNotBlank(message = "장소 ID는 빈 문자열이 될 수 없습니다.")
    val placeId: String,
) {
    fun toEntity(user: User): InterestPlace = InterestPlace(0L, placeId, user)
}
