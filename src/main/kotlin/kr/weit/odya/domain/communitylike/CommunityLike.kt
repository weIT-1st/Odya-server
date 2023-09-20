package kr.weit.odya.domain.communitylike

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.ManyToOne
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Entity
@IdClass(CommunityLikeId::class)
class CommunityLike(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    val community: Community,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    val user: User,
) : BaseTimeEntity()
