package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.placeReview.PlaceReviewSortType
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.PlaceReviewService
import kr.weit.odya.service.dto.AverageRatingResponse
import kr.weit.odya.service.dto.ExistReviewResponse
import kr.weit.odya.service.dto.PlaceReviewCreateRequest
import kr.weit.odya.service.dto.PlaceReviewListResponse
import kr.weit.odya.service.dto.PlaceReviewUpdateRequest
import kr.weit.odya.service.dto.ReviewCountResponse
import kr.weit.odya.service.dto.SliceResponse
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
import org.springframework.web.bind.annotation.RequestParam
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
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        placeReviewService.createReview(request, userId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PatchMapping
    fun updateReview(
        @Valid
        @RequestBody
        request: PlaceReviewUpdateRequest,
        @LoginUserId userId: Long,
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
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        placeReviewService.deleteReview(placeReviewId, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/places/{id}")
    fun getByPlaceReviewList(
        @NotBlank(message = "장소 ID는 필수 입력값입니다.")
        @PathVariable("id")
        placeId: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST") sortType: PlaceReviewSortType,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<PlaceReviewListResponse>> {
        return ResponseEntity.ok(placeReviewService.getByPlaceReviewList(placeId, size, sortType, lastId))
    }

    @GetMapping("/users/{id}")
    fun getByUserReviewList(
        @NotNull(message = "유저 ID는 필수 입력값입니다.")
        @Positive(message = "유저 ID는 양수여야 합니다.")
        @PathVariable("id")
        userId: Long,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST")
        sortType: PlaceReviewSortType,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<PlaceReviewListResponse>> {
        return ResponseEntity.ok(placeReviewService.getByUserReviewList(userId, size, sortType, lastId))
    }

    @GetMapping("/average/{id}")
    fun getAverageStarRating(
        @NotBlank(message = "장소 ID는 필수 입력값입니다.")
        @PathVariable("id")
        placeId: String,
    ): ResponseEntity<AverageRatingResponse> {
        return ResponseEntity.ok(placeReviewService.getAverageStarRating(placeId))
    }

    @GetMapping("/{id}")
    fun getExistReview(
        @NotBlank(message = "장소 ID는 필수 입력값입니다.")
        @PathVariable("id")
        placeId: String,
        @LoginUserId userId: Long,
    ): ResponseEntity<ExistReviewResponse> {
        return ResponseEntity.ok(placeReviewService.getExistReview(userId, placeId))
    }

    @GetMapping("/count/{id}")
    fun getReviewCount(
        @NotBlank(message = "장소 ID는 필수 입력값입니다.")
        @PathVariable("id")
        placeId: String,
    ): ResponseEntity<ReviewCountResponse> {
        return ResponseEntity.ok(placeReviewService.getReviewCount(placeId))
    }
}
