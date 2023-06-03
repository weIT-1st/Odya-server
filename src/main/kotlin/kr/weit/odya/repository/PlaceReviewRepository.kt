package kr.weit.odya.repository

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaceReviewRepository : JpaRepository<PlaceReview, PlaceReviewId> {
    fun findAllByPlaceId(placeId: String): List<PlaceReview>
    fun findAllByUserId(userId: Long): List<PlaceReview>
}
