package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.PlaceReviewService
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/place-reviews")
class PlaceReviewController(private val placeReviewService: PlaceReviewService) {
    @PostMapping
    fun createReview(
        @Valid
        @RequestBody
        request: PlaceReviewCreateRequest,
        @LoginUserId userId: Long
    ): ResponseEntity<Void> {
        placeReviewService.createReview(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PatchMapping
    fun updateReview(
        @Valid
        @RequestBody
        request: PlaceReviewUpdateRequest,
        @LoginUserId userId: Long
    ): ResponseEntity<Void> {
        placeReviewService.updateReview(request, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping("/{id}")
    fun deleteReview(
        @NotNull(message = "장소 리뷰 ID는 필수 입력값입니다.")
        @Positive(message = "장소 리뷰 ID는 양수여야 합니다.")
        @PathVariable("id")
        placeReviewId: Long,
        @LoginUserId userId: Long
    ): ResponseEntity<Void> {
        placeReviewService.deleteReview(placeReviewId, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/list/place/{id}")
    fun getPlaceReviewList(
        @NotNull(message = "장소 ID는 필수 입력값입니다.")
        @PathVariable("id")
        placeId: String,
        @LoginUserId userId: Long
    ): ResponseEntity<Void> {
        val placeReviewList = placeReviewService.getPlaceReviewList(placeId, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/list/user/")
    fun getUserReviewList(
        @LoginUserId userId: Long
    ): ResponseEntity<Void> {
        val placeReviewList = placeReviewService.getUserReviewList(userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
