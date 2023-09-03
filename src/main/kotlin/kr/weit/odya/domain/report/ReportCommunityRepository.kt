package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportCommunityRepository : JpaRepository<ReportCommunity, Long> {
    fun existsByCommunityIdAndUserId(communityId: Long, userId: Long): Boolean

    fun countAllByCommunityId(communityId: Long): Int

    fun deleteAllByCommunityId(communityId: Long)
}
