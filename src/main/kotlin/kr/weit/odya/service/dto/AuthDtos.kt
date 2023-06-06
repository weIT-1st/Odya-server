package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import kr.weit.odya.domain.user.Gender
import java.time.LocalDate

data class LoginRequest(
    @field:NotBlank
    val idToken: String
)

data class RegisterRequest(
    @field:NotBlank
    val idToken: String,

    val email: String?,

    val phoneNumber: String?,

    @field:NotBlank
    @field:Pattern(regexp = "[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9\\s]+")
    val nickname: String,

    @field:NotNull
    val gender: Gender,

    val birthday: LocalDate
)
