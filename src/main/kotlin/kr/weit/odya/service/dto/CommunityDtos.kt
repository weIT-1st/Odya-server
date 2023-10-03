package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.community.CommunityInformation
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
        communityContentImages: List<CommunityContentImage>,
    ): Community = Community(
        topic = topic,
        travelJournal = travelJournal,
        user = user,
        communityContentImages = communityContentImages,
        communityInformation = CommunityInformation(
            content = content,
            visibility = visibility,
            placeId = placeId,
        ),
    )
}

// 화면 최적화를 위해 댓글은 안 보냄 -> 프론트 측에서 스크롤을 내리면 그때 댓글을 가져가서 사용할 수 있음 + API 분리
data class CommunityResponse(
    val communityId: Long,
    val content: String,
    val visibility: CommunityVisibility,
    val placeId: String?,
    val travelJournal: TravelJournalSimpleResponse?,
    val topic: TopicResponse?,
    val communityContentImages: List<CommunityContentImageResponse>,
    val communityCommentCount: Int,
    val communityLikeCount: Int,
    val isUserLiked: Boolean,
) {
    companion object {
        fun from(
            community: Community,
            travelJournalSimpleResponse: TravelJournalSimpleResponse?,
            communityContentImages: List<CommunityContentImageResponse>,
            communityCommentCount: Int,
            isUserLiked: Boolean,
        ): CommunityResponse =
            CommunityResponse(
                communityId = community.id,
                content = community.communityInformation.content,
                visibility = community.communityInformation.visibility,
                placeId = community.communityInformation.placeId,
                travelJournal = travelJournalSimpleResponse,
                topic = community.topic?.let {
                    TopicResponse(it.id, it.word)
                },
                communityContentImages = communityContentImages,
                communityCommentCount = communityCommentCount,
                communityLikeCount = community.likeCount,
                isUserLiked = isUserLiked,
            )
    }
}

data class CommunityContentImageResponse(
    val communityContentImageId: Long,
    val imageUrl: String,
)

data class CommunitySummaryResponse(
    val communityId: Long,
    val communityMainImageUrl: String,
    val placeId: String?,
    val communityCommentCount: Int,
    val communityLikeCount: Int,
) {
    companion object {
        fun from(
            community: Community,
            communityMainImageUrl: String,
            communityCommentCount: Int,
        ): CommunitySummaryResponse =
            CommunitySummaryResponse(
                communityId = community.id,
                communityMainImageUrl = communityMainImageUrl,
                placeId = community.placeId,
                communityCommentCount = communityCommentCount,
                communityLikeCount = community.likeCount,
            )
    }
}

data class CommunityUpdateRequest(
    @field:NotBlank(message = "커뮤니티 글은 필수 입력 값입니다.")
    @field:Length(max = 200, message = "커뮤니티 글은 최대 200자까지 작성할 수 있습니다.")
    val content: String,
    @field:NotNull(message = "커뮤니티 공개 범위는 필수 입력 값입니다.")
    val visibility: CommunityVisibility,
    @field:NullOrNotBlank(message = "장소 ID는 빈 문자열이 될 수 없습니다.")
    val placeId: String?,
    val travelJournalId: Long?,
    val topicId: Long?,
    val deleteCommunityContentImageIds: List<Long>?,
) {
    fun toCommunityInformation() = CommunityInformation(
        content = content,
        visibility = visibility,
        placeId = placeId,
    )
}
