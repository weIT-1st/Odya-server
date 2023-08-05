package kr.weit.odya.service.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NullOrNotBlank
import org.hibernate.validator.constraints.Length

data class PlaceReviewCreateRequest(
    @field:NotNull(message = "장소는 필수 입력값입니다.")
    val placeId: String,

    @field:NotNull(message = "별점은 필수 입력값입니다.")
    @field:Min(message = "별점은 1점 이상이어야 합니다.", value = 1)
    @field:Max(message = "별점은 10점 이하이어야 합니다.", value = 10)
    val rating: Int,

    @field:NotBlank(message = "리뷰는 필수 입력값입니다.")
    @field:Length(max = 90, message = "리뷰의 최대 길이를 초과했습니다.")
    val review: String,
) {
    fun toEntity(user: User): PlaceReview = PlaceReview(
        placeId = placeId,
        user = user,
        starRating = rating,
        review = review,
        id = 0L,
    )
}

data class PlaceReviewUpdateRequest(
    @field:NotNull(message = "장소 리뷰 ID는 필수 입력값입니다.")
    @field:Positive(message = "장소 리뷰 ID는 양수여야 합니다.")
    val id: Long,

    @field:Min(message = "별점은 1점 이상이어야 합니다.", value = 1)
    @field:Max(message = "별점은 10점 이하이어야 합니다.", value = 10)
    val rating: Int?,

    @field:NullOrNotBlank(message = "리뷰는 빈 문자열이 될 수 없습니다.")
    @field:Length(max = 90, message = "리뷰의 최대 길이를 초과했습니다.")
    val review: String?,
)

data class PlaceReviewListResponse(
    val id: Long,
    val placeId: String,
    val userId: Long,
    val writerNickname: String,
    val starRating: Int,
    val review: String,
) {
    constructor(placeReview: PlaceReview) : this(
        placeReview.id,
        placeReview.placeId,
        placeReview.writerId,
        placeReview.writerNickname,
        placeReview.starRating,
        placeReview.review,
    )
}

data class SlicePlaceReviewResponse private constructor(
    override var hasNext: Boolean,
    val averageRating: Double,
    override val content: List<PlaceReviewListResponse>,
) : SliceResponse<PlaceReviewListResponse>(hasNext, content) {
    companion object {
        fun of(size: Int, content: List<PlaceReview>, averageRating: Double): SlicePlaceReviewResponse {
            val contents: List<PlaceReview>
            val hasNext: Boolean
            if (content.size > size) {
                hasNext = true
                contents = content.dropLast(1)
            } else {
                hasNext = false
                contents = content
            }
            return SlicePlaceReviewResponse(hasNext, averageRating, contents.map { PlaceReviewListResponse(it) })
        }
    }
}

data class ExistReviewResponse(
    val exist: Boolean,
)
