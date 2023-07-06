package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.profilecolor.NONE_PROFILE_COLOR_HEX
import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.domain.user.User

data class FollowRequest(
    @field:NotNull(message = "팔로우 할 USER ID는 필수 입력 값입니다")
    @field:Positive(message = "팔로우 할 USER ID는 양수여야 합니다.")
    val followingId: Long
)

data class FollowCountsResponse(
    val followingCount: Int,
    val followerCount: Int
)

data class FollowUserResponse(
    val userId: Long,
    val nickname: String,
    val profile: FollowProfileResponse
) {
    constructor(user: User, profileUrl: String) : this(
        user.id,
        user.nickname,
        FollowProfileResponse(
            profileUrl,
            if (user.profile.profileColor.colorHex != NONE_PROFILE_COLOR_HEX) {
                FollowProfileResponse.ProfileColorResponse(user.profile.profileColor)
            } else {
                null
            }
        )
    )
}

data class FollowProfileResponse(
    val profileUrl: String,
    val profileColor: ProfileColorResponse?
) {
    data class ProfileColorResponse(
        val colorHex: String,
        val red: Int,
        val green: Int,
        val blue: Int
    ) {
        constructor(profileColor: ProfileColor) : this(
            profileColor.colorHex,
            profileColor.red,
            profileColor.green,
            profileColor.blue
        )
    }
}
