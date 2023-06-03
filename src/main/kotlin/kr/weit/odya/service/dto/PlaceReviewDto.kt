package kr.weit.odya.service.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.user.User
import org.hibernate.validator.constraints.Length

class PlaceReviewDto(
    @field:NotNull(message = "장소는 필수 입력값입니다.")
    val placeId: String,

    @field:NotNull(message = "별점은 필수 입력값입니다.")
    @field:Min(message = "별점은 0점 이상이어야 합니다.", value = 0)
    @field:Max(message = "별점은 10점 이하이어야 합니다.", value = 10)
    val rating: Int,

    @field:NotBlank(message = "리뷰는 필수 입력값입니다.")
    @field:Length(max = 90, message = "리뷰의 최대 길이를 초과했습니다.")
    val comment: String
) {
    fun toEntity(user: User): PlaceReview {
        return PlaceReview(placeId, user, rating, comment)
    }
}
