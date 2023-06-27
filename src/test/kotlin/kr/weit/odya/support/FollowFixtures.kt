package kr.weit.odya.support

import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.PageResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

const val TEST_PAGE = 0
const val TEST_TOTAL_PAGE = 1
const val TEST_SIZE = 1
const val TEST_TOTAL_ELEMENT = 1
const val TEST_FOLLOWING_COUNT = 1
const val TEST_FOLLOWER_COUNT = 2

fun createFollowRequest(): FollowRequest = FollowRequest(TEST_OTHER_USER_ID)

fun createFollowCountsResponse(): FollowCountsResponse = FollowCountsResponse(TEST_FOLLOWING_COUNT, TEST_FOLLOWER_COUNT)

fun createFollowUserResponse() =
    FollowUserResponse(TEST_USER_ID, TEST_NICKNAME, TEST_PROFILE_NAME)

fun createFollowUserPage(): PageResponse<FollowUserResponse> =
    PageResponse(TEST_PAGE, TEST_TOTAL_PAGE, TEST_TOTAL_ELEMENT, listOf(createFollowUserResponse()))

fun createFollowPageable(): Pageable = PageRequest.of(TEST_PAGE, TEST_SIZE)

fun createFollowPage(follow: Follow = createFollow()): Page<Follow> =
    PageImpl(listOf(follow), createFollowPageable(), TEST_TOTAL_ELEMENT.toLong())

fun createFollow(follower: User = createUser(), following: User = createOtherUser()): Follow =
    Follow(follower = follower, following = following)
