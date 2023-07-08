package kr.weit.odya.support

import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.SliceResponse

const val TEST_TOTAL_ELEMENT = 1
const val TEST_FOLLOWING_COUNT = 1
const val TEST_FOLLOWER_COUNT = 2

fun createFollowRequest(): FollowRequest = FollowRequest(TEST_OTHER_USER_ID)

fun createFollowCountsResponse(): FollowCountsResponse = FollowCountsResponse(TEST_FOLLOWING_COUNT, TEST_FOLLOWER_COUNT)

fun createFollow(follower: User = createUser(), following: User = createOtherUser()): Follow =
    Follow(follower = follower, following = following)

fun createFollowUserResponse(
    userId: Long = TEST_USER_ID,
    nickname: String = TEST_NICKNAME,
    profileName: String = TEST_PROFILE_NAME,
) = FollowUserResponse(userId, nickname, profileName)

fun createFollowList(): List<Follow> = listOf(createFollow())

fun createFollowSlice(): SliceResponse<FollowUserResponse> =
    SliceResponse(
        TEST_PAGEABLE,
        listOf(
            createFollowUserResponse(),
            createFollowUserResponse(TEST_OTHER_USER_ID, TEST_OTHER_NICKNAME, TEST_OTHER_PROFILE_NAME),
        ),
    )
