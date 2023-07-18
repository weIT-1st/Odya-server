package kr.weit.odya.service.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import kr.weit.odya.client.kakao.KakaoUserInfo
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.support.exception.ErrorCode
import kr.weit.odya.support.validator.Nickname
import kr.weit.odya.support.validator.PhoneNumber
import java.time.LocalDate

data class AppleLoginRequest(
    @field:NotBlank
    val idToken: String,
)

data class KakaoLoginRequest(
    @field:NotBlank
    val accessToken: String,
)

data class KakaoRegistrationResponse(
    val username: String,
    val email: String?,
    val phoneNumber: String?,
    val nickname: String,
    val gender: Gender?,
) : ErrorResponse(ErrorCode.UNREGISTERED_USER) {
    constructor(kakaoUserInfo: KakaoUserInfo) : this(
        username = kakaoUserInfo.username,
        email = kakaoUserInfo.email,
        phoneNumber = kakaoUserInfo.phoneNumber,
        nickname = kakaoUserInfo.nickname,
        gender = kakaoUserInfo.gender,
    )
}

data class AppleRegisterRequest(
    @field:NotBlank
    val idToken: String,

    @field:Email
    override val email: String?,

    @field:PhoneNumber
    override val phoneNumber: String?,

    @field:Nickname
    override var nickname: String,

    @field:NotNull
    override var gender: Gender,

    @field:Past
    override var birthday: LocalDate,
) : RegisterRequest() {
    @JsonIgnore
    override var username: String = ""

    @JsonIgnore
    override var socialType: SocialType = SocialType.APPLE
    fun updateUsername(username: String) {
        this.username = username
    }
}

data class KakaoRegisterRequest(
    @field:NotBlank
    override var username: String,

    @field:Email
    override val email: String?,

    @field:PhoneNumber
    override val phoneNumber: String?,

    @field:Nickname
    override var nickname: String,

    @field:NotNull
    override var gender: Gender,

    @field:Past
    override var birthday: LocalDate,
) : RegisterRequest() {
    @JsonIgnore
    override var socialType: SocialType = SocialType.KAKAO
}

open class RegisterRequest {
    open val email: String? = null
    open val phoneNumber: String? = null
    open lateinit var username: String
        protected set
    open lateinit var nickname: String
        protected set
    open lateinit var gender: Gender
        protected set
    open lateinit var birthday: LocalDate
        protected set
    open lateinit var socialType: SocialType
        protected set
}

data class TokenResponse(
    val firebaseCustomToken: String,
)
