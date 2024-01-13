package kr.weit.odya.service

import kr.weit.odya.client.push.NotificationEventType
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.communitylike.CommunityLike
import kr.weit.odya.domain.communitylike.CommunityLikeId
import kr.weit.odya.domain.communitylike.CommunityLikeRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommunityLikeService(
    private val communityLikeRepository: CommunityLikeRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun increaseCommunityLikeCount(communityId: Long, userId: Long) {
        val community = communityRepository.getByCommunityId(communityId)
        val user = userRepository.getByUserId(userId)

        if (communityLikeRepository.existsById(CommunityLikeId(community, user))) {
            throw ExistResourceException("요청 사용자($userId)가 이미 좋아요를 누른 게시글($communityId)입니다.")
        }
        community.increaseLikeCount()
        communityLikeRepository.save(CommunityLike(community, user))
        publishLikePushEvent(user, community)
    }

    @Transactional
    fun decreaseCommunityLikeCount(communityId: Long, userId: Long) {
        val community = communityRepository.getByCommunityId(communityId)
        val user = userRepository.getByUserId(userId)

        community.decreaseLikeCount()
        communityLikeRepository.deleteById(CommunityLikeId(community, user))
    }

    private fun publishLikePushEvent(
        likeUser: User,
        community: Community,
    ) {
        val token = community.user.fcmToken ?: return
        eventPublisher.publishEvent(
            PushNotificationEvent(
                title = "오댜 알림",
                body = "${likeUser.nickname}님께 오댜를 받았습니다",
                tokens = listOf(token),
                userName = likeUser.nickname,
                eventType = NotificationEventType.COMMUNITY_LIKE,
                communityId = community.id,
            ),
        )
    }
}
