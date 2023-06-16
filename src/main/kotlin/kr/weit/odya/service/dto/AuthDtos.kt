package kr.weit.odya.service.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.support.validator.NicknameValidation
import kr.weit.odya.support.validator.PhoneNumberValidation
import java.time.LocalDate

data class LoginRequest(
    @field:NotBlank
    val idToken: String
)

data class RegisterRequest(
    @field:NotBlank
    val idToken: String,

    @field:Email
    val email: String?,

    @field:PhoneNumberValidation
    val phoneNumber: String?,

    @field:NicknameValidation
    val nickname: String,

    @field:NotNull
    val gender: Gender,

    @field:Past
    val birthday: LocalDate
)
