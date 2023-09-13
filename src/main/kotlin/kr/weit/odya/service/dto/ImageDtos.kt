package kr.weit.odya.service.dto

import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.support.validator.NullOrNotBlank
import org.hibernate.validator.constraints.Length

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
    @field:Length(max = 30, message = "장소명의 최대 길이를 초과했습니다.")
    val placeName: String?, // 이때 받은 내용으로 검색을 하거나 통계를 내지 않기 때문에 장소 id가 아니라 장소 명을 받는다
)
