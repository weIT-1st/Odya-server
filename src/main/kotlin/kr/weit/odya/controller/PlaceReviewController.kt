package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.PlaceReviewService
import kr.weit.odya.service.dto.PlaceReviewDto
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/place-review")
class PlaceReviewController(private val placeReviewService: PlaceReviewService) {
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun createReview(
        @Valid
        @RequestBody
        placeReview: PlaceReviewDto,
        user: User
    ) {
        placeReviewService.createReview(placeReview, user)
    }
}
