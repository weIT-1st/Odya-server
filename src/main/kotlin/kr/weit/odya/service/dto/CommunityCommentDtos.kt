package kr.weit.odya.service.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotBlank
import kr.weit.odya.domain.communitycomment.CommunityComment
import kr.weit.odya.domain.user.User
import java.time.LocalDateTime

data class CommunityCommentRequest(
    @field:NotBlank(message = "커뮤니티 댓글 내용은 비어있을 수 없습니다.")
    val content: String,
)

data class CommunityCommentResponse(
    val communityCommentId: Long,
    val content: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime,
    val isWriter: Boolean = false,
    val user: UserSimpleResponse,
) {
    constructor(
        communityComment: CommunityComment,
        user: User,
        profileUrl: String,
        isFollowing: Boolean,
    ) : this(
        communityComment.id,
        communityComment.content,
        communityComment.updatedDate,
        communityComment.community.user.id == user.id,
        UserSimpleResponse(user, profileUrl, isFollowing),
    )
}

data class CommunityCommentSimpleResponse(
    val communityCommentId: Long,
    val content: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime,
    val user: UserSimpleResponse,
)
