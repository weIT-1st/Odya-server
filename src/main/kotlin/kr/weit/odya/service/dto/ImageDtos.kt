package kr.weit.odya.service.dto

import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.support.validator.Latitude
import kr.weit.odya.support.validator.Longitude
import kr.weit.odya.support.validator.NullOrNotBlank
import org.hibernate.validator.constraints.Length

data class ImageResponse(
    val imageId: Long,
    val imageUrl: String,
    val placeId: String?,
    val isLifeShot: Boolean,
    val placeName: String?,
    val journalId: Long?,
    val communityId: Long?,
) {
    companion object {
        fun of(image: ContentImage, url: String) = ImageResponse(
            image.id,
            url,
            image.placeId,
            image.isLifeShot,
            image.placeName,
            image.travelJournalContentImage?.id,
            image.communityContentImage?.id,
        )
    }
}

data class LifeShotRequest(
    @field:NullOrNotBlank(message = "장소는 빈 문자열이 될 수 없습니다.")
    @field:Length(max = 30, message = "장소명의 최대 길이를 초과했습니다.")
    val placeName: String?, // 이때 받은 내용으로 검색을 하거나 통계를 내지 않기 때문에 장소 id가 아니라 장소 명을 받는다
)

data class CoordinateImageResponse(
    val imageId: Long,
    val userId: Long,
    val imageUrl: String,
    val placeId: String,
    val latitude: Double,
    val longitude: Double,
    val imageUserType: ImageUserType,
    val journalId: Long?,
    val communityId: Long?,
) {
    companion object {
        fun of(image: ContentImage, imageUserType: ImageUserType, url: String) = CoordinateImageResponse(
            image.id,
            image.user.id,
            url,
            image.placeId!!,
            image.coordinate!!.y,
            image.coordinate!!.x,
            imageUserType,
            image.travelJournalContentImage?.id,
            image.communityContentImage?.id,
        )
    }
}

enum class ImageUserType {
    USER, // 요청한 사용자 본인의 사진
    FRIEND, // 요청한 사용자의 친구의 사진
    OTHER, // 요청한 사용자의 친구가 아닌 사람의 사진
}

data class CoordinateImageRequest(
    @field:Longitude
    val leftLongitude: Double,
    @field:Latitude
    val bottomLatitude: Double,
    @field:Longitude
    val rightLongitude: Double,
    @field:Latitude
    val topLatitude: Double,
    @field:Positive(message = "사이즈는 양수여야 합니다.")
    val size: Int = 10,
)
