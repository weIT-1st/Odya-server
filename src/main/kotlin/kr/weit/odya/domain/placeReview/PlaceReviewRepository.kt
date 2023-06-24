package kr.weit.odya.domain.placeReview

import kr.weit.odya.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun PlaceReviewRepository.getByPlaceReviewId(id: Long): PlaceReview =
    findByIdOrNull(id) ?: throw NoSuchElementException("$id: 존재하지 않는 장소 리뷰입니다.")

fun PlaceReviewRepository.getByPlaceIdInitial(placeId: String, page: Pageable): Slice<PlaceReview> =
        findByPlaceId(placeId, page)

fun PlaceReviewRepository.getByPlaceIdStartId(placeId: String, startId: Long, page: Pageable): Slice<PlaceReview> =
        findByPlaceIdAndIdLessThan(placeId, startId, page)

fun PlaceReviewRepository.getByUserInitial(user: User, page: Pageable): Slice<PlaceReview> =
        findByUser(user, page)

fun PlaceReviewRepository.getByUserStartId(user: User, startId: Long, page: Pageable): Slice<PlaceReview> =
        findByUserAndIdLessThan(user, startId, page)

@Repository
interface PlaceReviewRepository : JpaRepository<PlaceReview, Long> {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean

    fun findByPlaceIdAndIdLessThan(placeId: String, startId: Long, page: Pageable): Slice<PlaceReview>

    fun findByPlaceId(placeId: String, page: Pageable): Slice<PlaceReview>

    fun findByUserAndIdLessThan(user: User, startId: Long, page: Pageable): Slice<PlaceReview>

    fun findByUser(user: User, page: Pageable): Slice<PlaceReview>
}
