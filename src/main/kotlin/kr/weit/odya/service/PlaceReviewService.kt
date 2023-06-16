package kr.weit.odya.service

import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
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
        placeReview.checkPermissions(userId)
        placeReview.apply {
            this.starRating = request.rating ?: this.starRating
            this.review = request.review ?: this.review
        }
        placeReviewRepository.save(placeReview)
    }

    @Transactional
    fun deleteReview(placeReviewId: Long, userId: Long) {
        val placeReview = placeReviewRepository.getByPlaceReviewId(placeReviewId)
        placeReview.checkPermissions(userId)
        placeReviewRepository.delete(placeReview)
    }

    fun getByPlaceReviewId(placeId: String): List<PlaceReviewListResponse> {
        return placeReviewRepository.findAllByPlaceId(placeId)
            .map { PlaceReviewListResponse(it) }
    }

    @Transactional
    fun getByUserReviewList(userId: Long): List<PlaceReviewListResponse> {
        val user: User = userRepository.getByUserId(userId)
        return placeReviewRepository.findAllByUser(user)
            .map { PlaceReviewListResponse(it) }
    }
}
