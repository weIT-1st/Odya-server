package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

fun ReportPlaceReviewRepository.existsByReviewAndUserId(placeReviewId: Long, userId: Long) =
    existsByPlaceReviewIdAndCommonReportInformationUserId(placeReviewId, userId)

fun ReportPlaceReviewRepository.deleteAllByUserId(userId: Long) {
    deleteAllByCommonReportInformationUserId(userId)
    deleteReportPlaceReviewByUserId(userId)
}

@Repository
interface ReportPlaceReviewRepository : JpaRepository<ReportPlaceReview, Long> {
    fun countAllByPlaceReviewId(placeReviewId: Long): Int

    fun deleteAllByPlaceReviewId(placeReviewId: Long)

    fun existsByPlaceReviewIdAndCommonReportInformationUserId(placeReviewId: Long, userId: Long): Boolean

    fun deleteAllByCommonReportInformationUserId(userId: Long)

    @Modifying
    @Query("delete from ReportPlaceReview rpr where rpr.placeReview.id in (select pr.id from PlaceReview pr where pr.user.id = :userId)")
    fun deleteReportPlaceReviewByUserId(userId: Long)
}
