package kr.weit.odya.domain.communitylike

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.NoArgsConstructor
import java.io.Serializable

@NoArgsConstructor
data class CommunityLikeId(
    val community: Community,
    val user: User,
) : Serializable
