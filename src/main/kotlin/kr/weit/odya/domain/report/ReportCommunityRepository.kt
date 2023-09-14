package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun ReportCommunityRepository.existsByCommunityIdAndUserId(communityId: Long, userId: Long) =
    existsByCommunityIdAndCommonReportInformationUserId(communityId, userId)

fun ReportCommunityRepository.deleteAllByUserId(userId: Long) =
    deleteAllByCommonReportInformationUserId(userId)

@Repository
interface ReportCommunityRepository : JpaRepository<ReportCommunity, Long> {
    fun existsByCommunityIdAndCommonReportInformationUserId(placeReviewId: Long, userId: Long): Boolean

    fun countAllByCommunityId(communityId: Long): Int

    fun deleteAllByCommunityId(communityId: Long)

    fun deleteAllByCommonReportInformationUserId(userId: Long)

    fun deleteAllByCommunityIn(communityIds: List<Long>)
}
