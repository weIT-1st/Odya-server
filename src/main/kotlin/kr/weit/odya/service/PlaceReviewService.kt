package kr.weit.odya.service

import kr.weit.odya.domain.user.User
import kr.weit.odya.repository.PlaceReviewRepository
import kr.weit.odya.service.dto.PlaceReviewDto
import org.springframework.stereotype.Service

@Service
class PlaceReviewService(private val placeReviewRepository: PlaceReviewRepository) {
    fun createReview(placeReview: PlaceReviewDto, user: User) {
        placeReviewRepository.save(placeReview.toEntity(user))
    }
}
