package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.community.CommunityVisibility
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NullOrNotBlank
import org.hibernate.validator.constraints.Length

data class CommunityCreateRequest(
    @field:NotBlank(message = "커뮤니티 글은 필수 입력 값입니다.")
    @field:Length(max = 200, message = "커뮤니티 글은 최대 200자까지 작성할 수 있습니다.")
    val content: String,
    @field:NotNull(message = "커뮤니티 공개 범위는 필수 입력 값입니다.")
    val visibility: CommunityVisibility,
    @field:NullOrNotBlank(message = "장소 ID는 빈 문자열이 될 수 없습니다.")
    val placeId: String?,
    val travelJournalId: Long?,
    val topicId: Long?,
) {
    fun toEntity(
        user: User,
        topic: Topic?,
        travelJournal: TravelJournal?,
        communityContentImages: List<CommunityContentImage>?,
    ): Community = Community(
        content = content,
        visibility = visibility,
        placeId = placeId,
        topic = topic,
        travelJournal = travelJournal,
        user = user,
        communityContentImages = communityContentImages ?: emptyList(),
    )
}
