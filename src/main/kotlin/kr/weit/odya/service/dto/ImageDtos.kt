package kr.weit.odya.service.dto

import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.support.validator.NullOrNotBlank

data class ImageResponse(
    val imageId: Long,
    val imageUrl: String,
    val placeId: String?,
    val isLifeShot: Boolean,
    val journalId: Long?,
    val communityId: Long?,
) {
    companion object {
        fun of(image: ContentImage, url: String) = ImageResponse(
            image.id,
            url,
            image.placeId,
            image.isLifeShot,
            image.travelJournalContentImage?.id,
            image.communityContentImage?.id,
        )
    }
}

data class LifeShotRequest(
    @field:NullOrNotBlank(message = "장소는 빈 문자열이 될 수 없습니다.")
    val placeId: String?,
)
