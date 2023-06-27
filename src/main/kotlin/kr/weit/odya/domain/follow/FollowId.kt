package kr.weit.odya.domain.follow

import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
data class FollowId(val follower: User, val following: User) : Serializable
