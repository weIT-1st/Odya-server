package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowerListBySearchCond
import kr.weit.odya.domain.follow.getFollowingListBySearchCond
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_DEFAULT_PAGEABLE
import kr.weit.odya.support.TEST_DEFAULT_SORT_TYPE
import kr.weit.odya.support.TEST_FOLLOWER_COUNT
import kr.weit.odya.support.TEST_FOLLOWING_COUNT
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createFollowCountsResponse
import kr.weit.odya.support.createFollowList
import kr.weit.odya.support.createFollowRequest
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createUser

class FollowServiceTest : DescribeSpec({
    val userRepository = mockk<UserRepository>()
    val followRepository = mockk<FollowRepository>()
    val followService = FollowService(followRepository, userRepository)

    describe("createFollow") {
        context("이미 존재하지 않는 팔로우를 생성하는 경우") {
            every { followRepository.existsByFollowerIdAndFollowingId(TEST_USER_ID, TEST_OTHER_USER_ID) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            every { userRepository.getByUserId(TEST_OTHER_USER_ID) } returns createOtherUser()
            every { followRepository.save(any()) } returns createFollow()
            it("정상적으로 종료한다.") {
                shouldNotThrow<Exception> { followService.createFollow(TEST_USER_ID, createFollowRequest()) }
            }
        }

        context("이미 존재하는 팔로우인 경우") {
            every { followRepository.existsByFollowerIdAndFollowingId(TEST_USER_ID, TEST_OTHER_USER_ID) } returns true
            it("[ExistResourceException] 반환한다.") {
                shouldThrow<ExistResourceException> { followService.createFollow(TEST_USER_ID, createFollowRequest()) }
            }
        }

        context("FOLLOWING ID의 사용자가 존재하지 않을 경우") {
            every { followRepository.existsByFollowerIdAndFollowingId(TEST_USER_ID, TEST_OTHER_USER_ID) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            every { userRepository.getByUserId(TEST_OTHER_USER_ID) } throws NoSuchElementException()
            it("[NoSuchElementException] 반환한다.") {
                shouldThrow<NoSuchElementException> { followService.createFollow(TEST_USER_ID, createFollowRequest()) }
            }
        }
    }

    describe("deleteFollow") {
        context("FOLLOWER ID와 FOLLOWING ID가 주어지는 경우") {
            every { followRepository.deleteByFollowerIdAndFollowingId(TEST_USER_ID, TEST_OTHER_USER_ID) } just runs
            it("정상적으로 종료한다.") {
                shouldNotThrow<Exception> { followService.deleteFollow(TEST_USER_ID, createFollowRequest()) }
            }
        }
    }

    describe("getSliceFollowings") {
        context("FOLLOWER ID, PAGEABLE, SORT TYPE 정보가 주어지는 경우") {
            every {
                followRepository.getFollowingListBySearchCond(
                    TEST_USER_ID,
                    TEST_DEFAULT_PAGEABLE,
                    TEST_DEFAULT_SORT_TYPE
                )
            } returns createFollowList()
            it("SliceResponse<FollowUserResponse>를 반환한다.") {
                val response =
                    followService.getSliceFollowings(TEST_USER_ID, TEST_DEFAULT_PAGEABLE, TEST_DEFAULT_SORT_TYPE)
                response.content[0].userId shouldBe TEST_OTHER_USER_ID
            }
        }
    }

    describe("getSliceFollowers") {
        context("FOLLOWER ID, PAGEABLE, SORT TYPE 정보가 주어지는 경우") {
            every {
                followRepository.getFollowerListBySearchCond(
                    TEST_USER_ID,
                    TEST_DEFAULT_PAGEABLE,
                    TEST_DEFAULT_SORT_TYPE
                )
            } returns createFollowList()
            it("SliceResponse<FollowUserResponse>를 반환한다.") {
                val response =
                    followService.getSliceFollowers(TEST_USER_ID, TEST_DEFAULT_PAGEABLE, TEST_DEFAULT_SORT_TYPE)
                response.content[0].userId shouldBe TEST_USER_ID
            }
        }
    }

    describe("getFollowCounts") {
        context("USER ID 정보가 주어지는 경우") {
            every { followRepository.countByFollowerId(TEST_USER_ID) } returns TEST_FOLLOWING_COUNT
            every { followRepository.countByFollowingId(TEST_USER_ID) } returns TEST_FOLLOWER_COUNT
            it("FollowCountsResponse를 반환한다.") {
                val response = followService.getFollowCounts(TEST_USER_ID)
                response shouldBe createFollowCountsResponse()
            }
        }
    }
})
