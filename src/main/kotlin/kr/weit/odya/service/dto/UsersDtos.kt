package kr.weit.odya.service.dto

import jakarta.validation.constraints.Pattern
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import java.time.LocalDate

data class UserResponse(
    val email: String?,
    val nickname: String,
    val phoneNumber: String?,
    val gender: Gender,
    val birthday: LocalDate,
    val socialType: SocialType
) {
    constructor(user: User) : this(
        user.email,
        user.nickname,
        user.phoneNumber,
        user.gender,
        user.birthday,
        user.socialType
    )
}

data class InformationRequest(
    @field:Pattern(regexp = "[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9\\s]+")
    val nickname: String
)
