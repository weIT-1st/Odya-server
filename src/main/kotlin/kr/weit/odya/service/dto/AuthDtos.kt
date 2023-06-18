package kr.weit.odya.service.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.support.validator.Nickname
import kr.weit.odya.support.validator.PhoneNumber
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

    @field:PhoneNumber
    val phoneNumber: String?,

    @field:Nickname
    val nickname: String,

    @field:NotNull
    val gender: Gender,

    @field:Past
    val birthday: LocalDate
)
