package kr.weit.odya.domain.placeReview

import kr.weit.odya.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun PlaceReviewRepository.getByPlaceReviewId(id: Long): PlaceReview =
    findByIdOrNull(id) ?: throw NoSuchElementException("$id: 존재하지 않는 장소 리뷰입니다.")

fun PlaceReviewRepository.getByPlaceIdInitialList(placeId: String, page: Pageable): Slice<PlaceReview> =
    findByPlaceIdOrderByIdDesc(placeId, page)

fun PlaceReviewRepository.getByPlaceIdStartIdList(placeId: String, startId: Long, page: Pageable): Slice<PlaceReview> =
    findByPlaceIdAndIdLessThanOrderByIdDesc(placeId, startId, page)

fun PlaceReviewRepository.getByUserInitialList(user: User, page: Pageable): Slice<PlaceReview> =
    findByUserOrderByIdDesc(user, page)

fun PlaceReviewRepository.getByUserStartIdList(user: User, startId: Long, page: Pageable): Slice<PlaceReview> =
    findByUserAndIdLessThanOrderByIdDesc(user, startId, page)

@Repository
interface PlaceReviewRepository : JpaRepository<PlaceReview, Long> {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean

    fun findByPlaceIdAndIdLessThanOrderByIdDesc(placeId: String, startId: Long, page: Pageable): Slice<PlaceReview>

    fun findByPlaceIdOrderByIdDesc(placeId: String, page: Pageable): Slice<PlaceReview>

    fun findByUserAndIdLessThanOrderByIdDesc(user: User, startId: Long, page: Pageable): Slice<PlaceReview>

    fun findByUserOrderByIdDesc(user: User, page: Pageable): Slice<PlaceReview>
}
