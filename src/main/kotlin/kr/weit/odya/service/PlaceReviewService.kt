package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import kr.weit.odya.service.dto.UserPlaceReviewListResponse
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
        if (placeReview.writerId != userId) {
            throw ForbiddenException("작성자만 수정할 수 있습니다.")
        }
        placeReview.apply {
            this.starRating = request.rating ?: this.starRating
            this.review = request.review ?: this.review
        }
        placeReviewRepository.save(placeReview)
    }

    @Transactional
    fun deleteReview(placeReviewId: Long, userId: Long) {
        val placeReview = placeReviewRepository.getByPlaceReviewId(placeReviewId)
        if (placeReview.writerId != userId) {
            throw ForbiddenException("작성자만 삭제할 수 있습니다.")
        }
        placeReviewRepository.delete(placeReview)
    }

    @Transactional
    fun getPlaceReviewList(placeId: String, userId: Long): MutableList<PlaceReviewListResponse>? {
        val placeReviewList: List<PlaceReview> = placeReviewRepository.findAllByPlaceId(placeId) ?: return null
        var responseList: MutableList<PlaceReviewListResponse> = mutableListOf()
        for (placeReview in placeReviewList) {
            responseList.add(PlaceReviewListResponse(placeReview))
        }
        return responseList
    }

    @Transactional
    fun getUserReviewList(userId: Long): MutableList<UserPlaceReviewListResponse> {
        val user: User = userRepository.getByUserId(userId)
        val placeReviewList: List<PlaceReview>? = placeReviewRepository.findAllByUser(user)
        var responseList: MutableList<UserPlaceReviewListResponse> = mutableListOf()
        for (placeReview in placeReviewList!!) {
            responseList.add(UserPlaceReviewListResponse(placeReview))
        }
        return responseList
    }
}
