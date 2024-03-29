package kr.weit.odya.service

import kr.weit.odya.client.push.NotificationEventType
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.FollowSortType
import kr.weit.odya.domain.follow.getByFollowerIdAndFollowingIdIn
import kr.weit.odya.domain.follow.getByFollowingIdAndFollowerIdIn
import kr.weit.odya.domain.follow.getFollowerListBySearchCond
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.follow.getFollowingListBySearchCond
import kr.weit.odya.domain.follow.getMayKnowFollowings
import kr.weit.odya.domain.follow.getVisitedFollowingIds
import kr.weit.odya.domain.profilecolor.NONE_PROFILE_COLOR_HEX
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.getByNickname
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.UserProfileResponse
import kr.weit.odya.service.dto.VisitedFollowingResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService,
    private val usersDocumentRepository: UsersDocumentRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun createFollow(followerId: Long, followRequest: FollowRequest) {
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followRequest.followingId)) {
            throw ExistResourceException("${followRequest.followingId}: 해당 유저를 이미 팔로우 중 입니다")
        }
        val follower = userRepository.getByUserId(followerId)
        val following = userRepository.getByUserId(followRequest.followingId)
        followRepository.save(Follow(follower = follower, following = following))
        publishFollowPushEvent(following, follower)
    }

    private fun publishFollowPushEvent(
        following: User,
        follower: User,
    ) {
        val token = following.fcmToken ?: return
        eventPublisher.publishEvent(
            PushNotificationEvent(
                title = "팔로우 알림",
                body = "${follower.nickname}님이 회원님을 팔로우했습니다",
                tokens = listOf(token),
                userName = follower.nickname,
                userProfileUrl = fileService.getPreAuthenticatedObjectUrl(follower.profile.profileName),
                userProfileColor = if (follower.profile.profileColor.colorHex != NONE_PROFILE_COLOR_HEX) {
                    UserProfileResponse.ProfileColorResponse(follower.profile.profileColor)
                } else {
                    null
                },
                eventType = NotificationEventType.FOLLOWER_ADD,
                followerId = follower.id,
            ),
        )
    }

    @Transactional
    fun deleteFollow(followerId: Long, followRequest: FollowRequest) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followRequest.followingId)
    }

    @Transactional
    fun deleteFollower(userId: Long, followerId: Long) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, userId)
    }

    @Transactional(readOnly = true)
    fun getSliceFollowings(
        loginUserId: Long,
        followerId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): SliceResponse<FollowUserResponse> {
        val followingIds = followRepository.getFollowingIds(loginUserId)
        val followingList = followRepository.getFollowingListBySearchCond(followerId, pageable, sortType)
            .map {
                FollowUserResponse(
                    it.following,
                    fileService.getPreAuthenticatedObjectUrl(it.following.profile.profileName),
                    it.following.id in followingIds,
                )
            }
        return SliceResponse(pageable, followingList)
    }

    @Transactional(readOnly = true)
    fun getSliceFollowers(
        loginUserId: Long,
        followingId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): SliceResponse<FollowUserResponse> {
        val followingIds = followRepository.getFollowingIds(loginUserId)
        val followerList = followRepository.getFollowerListBySearchCond(followingId, pageable, sortType)
            .map {
                FollowUserResponse(
                    it.follower,
                    fileService.getPreAuthenticatedObjectUrl(it.follower.profile.profileName),
                    it.follower.id in followingIds,
                )
            }
        return SliceResponse(pageable, followerList)
    }

    fun getFollowCounts(userId: Long): FollowCountsResponse {
        val followingCount = followRepository.countByFollowerId(userId)
        val followerCount = followRepository.countByFollowingId(userId)
        return FollowCountsResponse(followingCount, followerCount)
    }

    @Transactional(readOnly = true)
    fun searchByFollowingNickname(
        loginUserId: Long,
        userId: Long,
        nickname: String,
        size: Int,
        lastId: Long?,
    ): SliceResponse<FollowUserResponse> {
        val usersDocuments = usersDocumentRepository.getByNickname(nickname)
        val userIds = usersDocuments.map { it.id }
        val followingIds = followRepository.getFollowingIds(loginUserId)

        val followings =
            followRepository.getByFollowerIdAndFollowingIdIn(userId, userIds, size + 1, lastId).map {
                FollowUserResponse(
                    it.following,
                    fileService.getPreAuthenticatedObjectUrl(it.following.profile.profileName),
                    it.following.id in followingIds,
                )
            }
        return SliceResponse(size, followings)
    }

    @Transactional(readOnly = true)
    fun searchByFollowerNickname(
        loginUserId: Long,
        userId: Long,
        nickname: String,
        size: Int,
        lastId: Long?,
    ): SliceResponse<FollowUserResponse> {
        val usersDocuments = usersDocumentRepository.getByNickname(nickname)
        val userIds = usersDocuments.map { it.id }
        val followingIds = followRepository.getFollowingIds(loginUserId)
        val followers =
            followRepository.getByFollowingIdAndFollowerIdIn(userId, userIds, size + 1, lastId).map {
                FollowUserResponse(
                    it.follower,
                    fileService.getPreAuthenticatedObjectUrl(it.follower.profile.profileName),
                    it.follower.id in followingIds,
                )
            }
        return SliceResponse(size, followers)
    }

    @Transactional(readOnly = true)
    fun getMayKnowFollowings(userId: Long, size: Int, lastId: Long?): SliceResponse<FollowUserResponse> {
        val mayKnowFollowings = followRepository.getMayKnowFollowings(userId, size + 1, lastId)

        return SliceResponse(
            size,
            mayKnowFollowings.map {
                FollowUserResponse(
                    it,
                    fileService.getPreAuthenticatedObjectUrl(it.profile.profileName),
                    false,
                )
            },
        )
    }

    @Transactional
    fun getVisitedFollowings(placeID: String, userId: Long): VisitedFollowingResponse {
        val getVisitedFollowingIds = followRepository.getVisitedFollowingIds(placeID, userId)
        val followings = userRepository.getByUserIds(getVisitedFollowingIds.take(3)).map {
            FollowUserResponse(
                it,
                fileService.getPreAuthenticatedObjectUrl(it.profile.profileName),
                true,
            )
        }
        return VisitedFollowingResponse(getVisitedFollowingIds.size, followings)
    }
}
