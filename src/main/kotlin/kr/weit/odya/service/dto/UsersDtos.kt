package kr.weit.odya.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.NotBlank
import kr.weit.odya.domain.profilecolor.NONE_PROFILE_COLOR_HEX
import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.Nickname
import kr.weit.odya.support.validator.PhoneNumber
import java.time.LocalDate

data class UserResponse(
    val userId: Long,
    val email: String?,
    val nickname: String,
    val phoneNumber: String?,
    val gender: Gender,
    val birthday: LocalDate,
    val socialType: SocialType,
    val profile: UserProfileResponse,
) {
    constructor(user: User, profileUrl: String) : this(
        user.id,
        user.email,
        user.nickname,
        user.phoneNumber,
        user.gender,
        user.birthday,
        user.socialType,
        UserProfileResponse(
            profileUrl,
            if (user.profile.profileColor.colorHex != NONE_PROFILE_COLOR_HEX) {
                UserProfileResponse.ProfileColorResponse(user.profile.profileColor)
            } else {
                null
            },
        ),
    )
}

data class InformationRequest(
    @field:Nickname
    val nickname: String,
)

data class FCMTokenRequest(
    @field:NotBlank(message = "FCM Token은 필수입니다.")
    val fcmToken: String,
)

data class UserProfileResponse(
    val profileUrl: String,
    val profileColor: ProfileColorResponse?,
) {
    data class ProfileColorResponse(
        val colorHex: String,
        val red: Int,
        val green: Int,
        val blue: Int,
    ) {
        constructor(profileColor: ProfileColor) : this(
            profileColor.colorHex,
            profileColor.red,
            profileColor.green,
            profileColor.blue,
        )
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserSimpleResponse(
    val userId: Long,
    val nickname: String,
    val profile: ProfileResponse,
    val isFollowing: Boolean,
) {
    constructor(user: User, profileUrl: String, isFollowing: Boolean) : this(
        user.id,
        user.nickname,
        ProfileResponse(
            profileUrl,
            if (user.profile.profileColor.colorHex != NONE_PROFILE_COLOR_HEX) {
                ProfileResponse.ProfileColorResponse(user.profile.profileColor)
            } else {
                null
            },
        ),
        isFollowing,
    )
}

data class ProfileResponse(
    val profileUrl: String,
    val profileColor: ProfileColorResponse?,
) {
    data class ProfileColorResponse(
        val colorHex: String,
        val red: Int,
        val green: Int,
        val blue: Int,
    ) {
        constructor(profileColor: ProfileColor) : this(
            profileColor.colorHex,
            profileColor.red,
            profileColor.green,
            profileColor.blue,
        )
    }
}

data class UserStatisticsResponse(
    val travelJournalCount: Int,
    val travelPlaceCount: Int,
    val followingsCount: Int,
    val followersCount: Int,
    val odyaCount: Int,
    val lifeShotCount: Int,
)

data class SearchPhoneNumberRequest(
    @PhoneNumber
    val phoneNumber: String,
)
