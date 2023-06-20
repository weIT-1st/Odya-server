package kr.weit.odya.domain.follow

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Entity
@IdClass(FollowId::class)
class Follow(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", columnDefinition = "NUMERIC(19, 0)", nullable = false, updatable = false)
    val follower: User,

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", columnDefinition = "NUMERIC(19, 0)", nullable = false, updatable = false)
    val following: User
) : BaseTimeEntity()
