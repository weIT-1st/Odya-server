package kr.weit.odya.service.dto

import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NicknameValidation
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
    @field:NicknameValidation
    val nickname: String
)
