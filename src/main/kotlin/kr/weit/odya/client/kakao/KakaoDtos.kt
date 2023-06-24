package kr.weit.odya.client.kakao

import com.fasterxml.jackson.annotation.JsonProperty
import kr.weit.odya.domain.user.Gender

data class KakaoUserInfo(
        val id: String = "",
        @JsonProperty("kakao_account")
        val kakaoAccount: KakaoAccount = KakaoAccount()
) {
    data class KakaoAccount(
            val name: String? = null,
            val email: String? = null,
            val gender: String? = null,
            @JsonProperty("phone_number")
            val phoneNumber: String? = null,
            val profile: Profile = Profile()
    )

    data class Profile(
            val nickname: String = ""
    )

    val username: String = "KAKAO_$id"
    val name: String? = kakaoAccount.name
    val email: String? = kakaoAccount.email
    val phoneNumber: String? = kakaoAccount.phoneNumber
    val gender: Gender? = genderFormat()
    val nickname: String = kakaoAccount.profile.nickname

    private fun genderFormat(): Gender? =
            if (!kakaoAccount.gender.isNullOrBlank()) {
                Gender.valueOf(kakaoAccount.gender[0].toString().uppercase())
            } else {
                null
            }
}
