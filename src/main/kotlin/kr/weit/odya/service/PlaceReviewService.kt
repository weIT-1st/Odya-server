package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.PlaceReviewSortType
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.placeReview.getPlaceReviewListByPlaceId
import kr.weit.odya.domain.placeReview.getPlaceReviewListByUser
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.deleteAllByUserId
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.AverageRatingResponse
import kr.weit.odya.service.dto.ExistReviewResponse
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import kr.weit.odya.service.dto.ReviewCountResponse
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.roundToInt

@Service
class PlaceReviewService(
    private val placeReviewRepository: PlaceReviewRepository,
    private val userRepository: UserRepository,
    private val reportPlaceReviewRepository: ReportPlaceReviewRepository,
    private val fileService: FileService,
    private val followRepository: FollowRepository,
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
        reportPlaceReviewRepository.deleteAllByPlaceReviewId(placeReviewId)
        placeReviewRepository.deleteById(placeReviewId)
    }

    @Transactional
    fun getByPlaceReviewList(
        userId: Long,
        placeId: String,
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): SliceResponse<PlaceReviewListResponse> {
        val followingIdList = followRepository.getFollowingIds(userId)
        val placeReviewListResponses = placeReviewRepository.getPlaceReviewListByPlaceId(placeId, size, sortType, lastId).map { placeReview ->
            PlaceReviewListResponse(placeReview, fileService.getPreAuthenticatedObjectUrl(placeReview.user.profile.profileName), placeReview.writerId in followingIdList)
        }
        return SliceResponse(
            size,
            placeReviewListResponses,
        )
    }

    @Transactional
    fun getByUserReviewList(
        loginUserId: Long,
        userId: Long,
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): SliceResponse<PlaceReviewListResponse> {
        val user: User = userRepository.getByUserId(userId)
        val isFollowing = followRepository.existsByFollowerIdAndFollowingId(loginUserId, userId)
        val placeReviewListResponses = placeReviewRepository.getPlaceReviewListByUser(user, size, sortType, lastId).map { placeReview ->
            PlaceReviewListResponse(placeReview, fileService.getPreAuthenticatedObjectUrl(placeReview.user.profile.profileName), isFollowing)
        }
        return SliceResponse(
            size,
            placeReviewListResponses,
        )
    }

    fun getAverageStarRating(placeId: String): AverageRatingResponse {
        return AverageRatingResponse(getAverage(placeReviewRepository.getAverageRatingByPlaceId(placeId)))
    }

    fun getExistReview(userId: Long, placeId: String): ExistReviewResponse {
        return ExistReviewResponse(placeReviewRepository.existsByUserIdAndPlaceId(userId, placeId))
    }

    fun getReviewCount(placeId: String): ReviewCountResponse {
        return ReviewCountResponse(placeReviewRepository.countByPlaceId(placeId))
    }

    @Transactional
    fun deleteReviewRelatedData(userId: Long) {
        reportPlaceReviewRepository.deleteAllByUserId(userId)
        placeReviewRepository.deleteByUserId(userId)
    }

    private fun checkPermissions(placeReview: PlaceReview, userId: Long) {
        if (placeReview.writerId != userId) {
            throw ForbiddenException("권한 없음")
        }
    }

    private fun getAverage(averageRating: Double?): Double {
        return ((averageRating ?: 0.0) * 10).roundToInt() / 10.0
    }
}
