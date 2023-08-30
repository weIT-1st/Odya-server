package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportPlaceReviewRepository : JpaRepository<ReportPlaceReview, Long> {
    fun countAllByPlaceReviewId(placeReviewId: Long): Int

    fun deleteByPlaceReviewId(placeReviewId: Long)

    fun existsByPlaceReviewIdAndUserId(placeReviewId: Long, userId: Long): Boolean
}
