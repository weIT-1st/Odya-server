package kr.weit.odya.service

import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.FollowSortType
import kr.weit.odya.domain.follow.getByFollowerIdAndFollowingIdIn
import kr.weit.odya.domain.follow.getFollowerListBySearchCond
import kr.weit.odya.domain.follow.getFollowingListBySearchCond
import kr.weit.odya.domain.follow.getMayKnowFollowings
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.getByNickname
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService,
    private val usersDocumentRepository: UsersDocumentRepository,
    private val firebaseCloudMessageService: FirebaseCloudMessageService,
) {
    @Transactional
    fun createFollow(followerId: Long, followRequest: FollowRequest) {
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followRequest.followingId)) {
            throw ExistResourceException("${followRequest.followingId}: 해당 유저를 이미 팔로우 중 입니다")
        }
        val follower = userRepository.getByUserId(followerId)
        val following = userRepository.getByUserId(followRequest.followingId)
        followRepository.save(Follow(follower = follower, following = following))
        sendPushNotification(following, follower)
    }

    private fun sendPushNotification(
        following: User,
        follower: User,
    ) {
        val token = following.fcmToken ?: return
        firebaseCloudMessageService.sendPushNotification(
            PushNotificationRequest(
                title = "팔로우 알림",
                body = "${follower.nickname}님이 팔로우 했어요!",
                token = token,
                data = mapOf("followerId" to follower.id.toString()),
            ),
        )
    }

    @Transactional
    fun deleteFollow(followerId: Long, followRequest: FollowRequest) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followRequest.followingId)
    }

    @Transactional(readOnly = true)
    fun getSliceFollowings(
        followerId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): SliceResponse<FollowUserResponse> {
        val followingList = followRepository.getFollowingListBySearchCond(followerId, pageable, sortType)
            .map {
                FollowUserResponse(
                    it.following,
                    fileService.getPreAuthenticatedObjectUrl(it.following.profile.profileName),
                )
            }
        return SliceResponse(pageable, followingList)
    }

    @Transactional(readOnly = true)
    fun getSliceFollowers(
        followingId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): SliceResponse<FollowUserResponse> {
        val followerList = followRepository.getFollowerListBySearchCond(followingId, pageable, sortType)
            .map {
                FollowUserResponse(
                    it.follower,
                    fileService.getPreAuthenticatedObjectUrl(it.follower.profile.profileName),
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
    fun searchByNickname(userId: Long, nickname: String, size: Int, lastId: Long?): SliceResponse<FollowUserResponse> {
        val usersDocuments = usersDocumentRepository.getByNickname(nickname)
        val userIds = usersDocuments.map { it.id }

        val followings =
            followRepository.getByFollowerIdAndFollowingIdIn(userId, userIds, size + 1, lastId).map {
                FollowUserResponse(
                    it.following,
                    fileService.getPreAuthenticatedObjectUrl(it.following.profile.profileName),
                )
            }
        return SliceResponse(size, followings)
    }

    @Transactional(readOnly = true)
    fun getMayKnowFollowings(userId: Long, size: Int, lastId: Long?): SliceResponse<FollowUserResponse> {
        val mayKnowFollowings = followRepository.getMayKnowFollowings(userId, size + 1, lastId)

        return SliceResponse(
            size,
            mayKnowFollowings.map {
                FollowUserResponse(
                    it.following,
                    fileService.getPreAuthenticatedObjectUrl(it.following.profile.profileName),
                )
            },
        )
    }
}
