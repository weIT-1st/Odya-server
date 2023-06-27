package kr.weit.odya.service

import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.FollowSortType
import kr.weit.odya.domain.follow.getFollowerListBySearchCond
import kr.weit.odya.domain.follow.getFollowingListBySearchCond
import kr.weit.odya.domain.user.UserRepository
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
    private val userRepository: UserRepository
) {
    @Transactional
    fun createFollow(followerId: Long, followRequest: FollowRequest) {
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followRequest.followingId)) {
            throw ExistResourceException("${followRequest.followingId}: 해당 유저를 이미 팔로우 중 입니다")
        }
        val follower = userRepository.getByUserId(followerId)
        val following = userRepository.getByUserId(followRequest.followingId)
        followRepository.save(Follow(follower = follower, following = following))
    }

    @Transactional
    fun deleteFollow(followerId: Long, followRequest: FollowRequest) {
        followRepository.deleteByFollowerIdAndFollowingId(followerId, followRequest.followingId)
    }

    @Transactional(readOnly = true)
    fun getSliceFollowings(
        followerId: Long,
        pageable: Pageable,
        sortType: FollowSortType
    ): SliceResponse<FollowUserResponse> {
        val followingList = followRepository.getFollowingListBySearchCond(followerId, pageable, sortType)
            .map { FollowUserResponse(it.following) }
        return SliceResponse(pageable, followingList)
    }

    @Transactional(readOnly = true)
    fun getSliceFollowers(
        followingId: Long,
        pageable: Pageable,
        sortType: FollowSortType
    ): SliceResponse<FollowUserResponse> {
        val followerList = followRepository.getFollowerListBySearchCond(followingId, pageable, sortType)
            .map { FollowUserResponse(it.follower) }
        return SliceResponse(pageable, followerList)
    }

    fun getFollowCounts(userId: Long): FollowCountsResponse {
        val followingCount = followRepository.countByFollowerId(userId)
        val followerCount = followRepository.countByFollowingId(userId)
        return FollowCountsResponse(followingCount, followerCount)
    }
}
