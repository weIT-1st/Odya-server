package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.user.User

data class FollowRequest(
    @field:NotNull(message = "팔로우 할 USER ID는 필수 입력 값입니다")
    @field:Positive(message = "팔로우 할 USER ID는 양수여야 합니다.")
    val followingId: Long,
)

data class FollowCountsResponse(
    val followingCount: Int,
    val followerCount: Int,
)

data class FollowUserResponse(
    val userId: Long,
    val nickname: String,
    val profileName: String,
) {
    constructor(user: User) : this(
        user.id,
        user.nickname,
        user.profileName,
    )
}
