package kr.weit.odya.domain.placeReview

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun PlaceReviewRepository.getByPlaceReviewId(id: Long): PlaceReview =
    findByIdOrNull(id) ?: throw NoSuchElementException("$id: 존재하지 않는 장소 리뷰입니다.")

@Repository
interface PlaceReviewRepository : JpaRepository<PlaceReview, Long> {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean
}
