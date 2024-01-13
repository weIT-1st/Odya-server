package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.client.push.NotificationEventType
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.communitycomment.CommunityComment
import kr.weit.odya.domain.communitycomment.CommunityCommentRepository
import kr.weit.odya.domain.communitycomment.getCommunityCommentBy
import kr.weit.odya.domain.communitycomment.getSliceCommunityCommentBy
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.CommunityCommentRequest
import kr.weit.odya.service.dto.CommunityCommentResponse
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommunityCommentService(
    private val communityRepository: CommunityRepository,
    private val communityCommentRepository: CommunityCommentRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService,
    private val followRepository: FollowRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun createCommunityComment(userId: Long, communityId: Long, request: CommunityCommentRequest): Long {
        val community = communityRepository.getByCommunityId(communityId)
        val user = userRepository.getByUserId(userId)
        val communityComment = communityCommentRepository.save(
            CommunityComment(
                content = request.content,
                user = user,
                community = community,
            ),
        )
        publishCommentPushEvent(user, community, communityComment)
        return communityComment.id
    }

    @Transactional(readOnly = true)
    fun getCommunityComments(userId: Long, communityId: Long, size: Int, lastId: Long?): SliceResponse<CommunityCommentResponse> {
        val followingIdList = followRepository.getFollowingIds(userId)
        val communityCommentResponses =
            communityCommentRepository.getSliceCommunityCommentBy(communityId, size, lastId).map {
                val profileUrl = fileService.getPreAuthenticatedObjectUrl(it.user.profile.profileName)
                CommunityCommentResponse(it, it.user, profileUrl, it.id in followingIdList)
            }

        return SliceResponse(size, communityCommentResponses)
    }

    @Transactional
    fun updateCommunityComment(
        userId: Long,
        communityId: Long,
        communityCommentId: Long,
        request: CommunityCommentRequest,
    ) {
        val communityComment = getVerifiedCommunityComment(communityCommentId, communityId, userId)
        communityComment.updateContent(request.content)
    }

    @Transactional
    fun deleteCommunityComment(userId: Long, communityId: Long, communityCommentId: Long) {
        val communityComment = getVerifiedCommunityComment(communityCommentId, communityId, userId)
        communityCommentRepository.delete(communityComment)
    }

    private fun getVerifiedCommunityComment(communityCommentId: Long, communityId: Long, userId: Long) =
        communityCommentRepository.getCommunityCommentBy(communityCommentId, communityId).apply {
            validateUserPermission(this, userId)
        }

    private fun validateUserPermission(communityComment: CommunityComment, userId: Long) {
        if (communityComment.user.id != userId) {
            throw ForbiddenException("요청 사용자($userId)는 해당 커뮤니티 댓글(${communityComment.id})을 처리할 권한이 없습니다.")
        }
    }

    private fun publishCommentPushEvent(
        commentWriter: User,
        community: Community,
        communityComment: CommunityComment,
    ) {
        val token = community.user.fcmToken ?: return
        eventPublisher.publishEvent(
            PushNotificationEvent(
                title = "댓글 알림",
                body = "${commentWriter.nickname}님이 댓글을 남겼습니다 : ${communityComment.content}",
                tokens = listOf(token),
                userName = commentWriter.nickname,
                eventType = NotificationEventType.COMMUNITY_COMMENT,
                communityId = community.id,
                content = communityComment.content,
            ),
        )
    }
}
