package kr.weit.odya.domain.report

import kr.weit.odya.domain.placeReview.PlaceReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportPlaceReviewRepository : JpaRepository<ReportPlaceReview, Long> {
    fun countAllByPlaceReviewId(placeReviewId: Long): Int

    fun deleteByPlaceReviewId(placeReviewId: Long)

    fun existsByPlaceReviewIdAndUserId(placeReviewId: Long, userId: Long): Boolean

    fun deleteAllByPlaceReview(placeReview: PlaceReview)

    fun deleteAllByUserId(userId: Long)

    fun deleteAllByPlaceReviewIn(placeReviews: List<PlaceReview>)
}
