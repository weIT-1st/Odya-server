package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceIdInitialList
import kr.weit.odya.domain.placeReview.getByPlaceIdStartIdList
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.placeReview.getByUserInitialList
import kr.weit.odya.domain.placeReview.getByUserStartIdList
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.ConversionPlaceReview
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PlaceReviewService(
    private val placeReviewRepository: PlaceReviewRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun createReview(request: PlaceReviewCreateRequest, userId: Long) {
        val user: User = userRepository.getByUserId(userId)
        if (placeReviewRepository.existsByUserIdAndPlaceId(userId, request.placeId)) {
            throw ExistResourceException("이미 리뷰를 작성한 장소입니다.")
        }
        placeReviewRepository.save(request.toEntity(user))
    }

    @Transactional
    fun updateReview(request: PlaceReviewUpdateRequest, userId: Long) {
        val placeReview = placeReviewRepository.getByPlaceReviewId(request.id)
        checkPermissions(placeReview, userId)
        placeReview.apply {
            this.starRating = request.rating ?: this.starRating
            this.review = request.review ?: this.review
        }
        placeReviewRepository.save(placeReview)
    }

    @Transactional
    fun deleteReview(placeReviewId: Long, userId: Long) {
        val placeReview = placeReviewRepository.getByPlaceReviewId(placeReviewId)
        checkPermissions(placeReview, userId)
        placeReviewRepository.delete(placeReview)
    }

    private fun checkPermissions(placeReview: PlaceReview, userId: Long) {
        if (placeReview.writerId != userId) {
            throw ForbiddenException("권한 없음")
        }
    }

    fun getByPlaceReviewList(placeId: String, startId: Long?, count: Int): PlaceReviewListResponse {
        val placeReviews = if (startId == null) {
            placeReviewRepository.getByPlaceIdInitialList(placeId, PageRequest.of(0, count))
        } else {
            placeReviewRepository.getByPlaceIdStartIdList(placeId, startId, PageRequest.of(0, count))
        }
        return PlaceReviewListResponse(placeReviews.content.map { ConversionPlaceReview(it) }, placeReviews.last().id, placeReviews.isLast)
    }

    @Transactional
    fun getByUserReviewList(userId: Long, startId: Long?, count: Int): PlaceReviewListResponse {
        val user: User = userRepository.getByUserId(userId)
        val placeReviews = if (startId == null) {
            placeReviewRepository.getByUserInitialList(user, PageRequest.of(0, count))
        } else {
            placeReviewRepository.getByUserStartIdList(user, startId, PageRequest.of(0, count))
        }
        return PlaceReviewListResponse(placeReviews.content.map { ConversionPlaceReview(it) }, placeReviews.last().id, placeReviews.isLast)
    }
}
