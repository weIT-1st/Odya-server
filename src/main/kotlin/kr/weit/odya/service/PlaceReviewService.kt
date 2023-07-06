package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.PlaceReviewSortType
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.placeReview.getPlaceReviewListByPlaceId
import kr.weit.odya.domain.placeReview.getPlaceReviewListByUser
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.data.domain.Pageable
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

    fun getByPlaceReviewList(placeId: String, pageable: Pageable, sortType: PlaceReviewSortType): SliceResponse<PlaceReviewListResponse> {
        val placeReviews = placeReviewRepository.getPlaceReviewListByPlaceId(placeId, pageable, sortType).map { PlaceReviewListResponse(it) }
        return SliceResponse(pageable, placeReviews)
    }

    @Transactional
    fun getByUserReviewList(userId: Long, pageable: Pageable, sortType: PlaceReviewSortType): SliceResponse<PlaceReviewListResponse> {
        val user: User = userRepository.getByUserId(userId)
        val placeReviews = placeReviewRepository.getPlaceReviewListByUser(user, pageable, sortType).map { PlaceReviewListResponse(it) }
        return SliceResponse(pageable, placeReviews)
    }
}
