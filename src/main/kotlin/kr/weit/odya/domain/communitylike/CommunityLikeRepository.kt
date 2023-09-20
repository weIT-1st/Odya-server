package kr.weit.odya.domain.communitylike

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommunityLikeRepository : JpaRepository<CommunityLike, CommunityLikeId> {
    fun countByUserId(userId: Long): Int
}
