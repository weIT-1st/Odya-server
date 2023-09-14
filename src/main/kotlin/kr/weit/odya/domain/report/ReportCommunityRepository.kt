package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

fun ReportCommunityRepository.existsByCommunityIdAndUserId(communityId: Long, userId: Long) =
    existsByCommunityIdAndCommonReportInformationUserId(communityId, userId)

fun ReportCommunityRepository.deleteAllByUserId(userId: Long) {
    deleteAllByCommonReportInformationUserId(userId)
    deleteReportCommunitiesByUserId(userId)
}

@Repository
interface ReportCommunityRepository : JpaRepository<ReportCommunity, Long> {
    fun existsByCommunityIdAndCommonReportInformationUserId(placeReviewId: Long, userId: Long): Boolean

    fun countAllByCommunityId(communityId: Long): Int

    fun deleteAllByCommunityId(communityId: Long)

    fun deleteAllByCommonReportInformationUserId(userId: Long)

    fun deleteAllByCommunityIdIn(communityIds: List<Long>)

    @Modifying
    @Query("delete from ReportCommunity rc where rc.community.id in (select c.id from Community c where c.user.id = :userId)")
    fun deleteReportCommunitiesByUserId(userId: Long)
}
