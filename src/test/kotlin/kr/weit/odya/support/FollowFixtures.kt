package kr.weit.odya.support

import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowProfileResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.VisitedFollowingResponse

const val TEST_FOLLOWING_COUNT = 1
const val TEST_FOLLOWER_COUNT = 2
const val TEST_VISITED_FOLLOWING_COUNT = 2
const val TEST_FCM_TOKEN = "testFcmToken"

fun createFollowRequest(): FollowRequest = FollowRequest(TEST_OTHER_USER_ID)

fun createFollowCountsResponse(): FollowCountsResponse = FollowCountsResponse(TEST_FOLLOWING_COUNT, TEST_FOLLOWER_COUNT)

fun createFollow(follower: User = createUser(), following: User = createOtherUser()): Follow =
    Follow(follower = follower, following = following)

fun createFollowUserResponse(
    userId: Long = TEST_USER_ID,
    nickname: String = TEST_NICKNAME,
    isFollowing: Boolean = true,
) = FollowUserResponse(userId, nickname, createFollowProfileResponse(), isFollowing)

fun createFollowList(): List<Follow> = listOf(createFollow())

fun createFollowSlice(): SliceResponse<FollowUserResponse> =
    SliceResponse(
        TEST_PAGEABLE,
        listOf(
            createFollowUserResponse(),
            createFollowUserResponse(TEST_OTHER_USER_ID, TEST_OTHER_NICKNAME),
        ),
    )

fun createFollowProfileResponse(): FollowProfileResponse =
    FollowProfileResponse(TEST_PROFILE_URL, createFollowProfileColorResponse())

fun createFollowProfileColorResponse(): FollowProfileResponse.ProfileColorResponse =
    FollowProfileResponse.ProfileColorResponse(createProfileColor(TEST_PROFILE_COLOR_ID))

fun createFollowerFcmTokenList() = listOf(TEST_FCM_TOKEN)

fun createVisitedFollowingResponse(): VisitedFollowingResponse =
    VisitedFollowingResponse(
        TEST_VISITED_FOLLOWING_COUNT,
        listOf(
            createFollowUserResponse(),
            createFollowUserResponse(TEST_OTHER_USER_ID, TEST_OTHER_NICKNAME),
        ),
    )

fun createVisitedFollowingIds() = listOf(TEST_OTHER_USER_ID, TEST_ANOTHER_USER_ID)
