package kr.weit.odya.support

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.communitylike.CommunityLike
import kr.weit.odya.domain.user.User

const val TEST_ODYA_COUNT = 2

fun createCommunityLike(community: Community = createCommunity(), user: User = createUser()) = CommunityLike(
    community = community,
    user = user,
)
