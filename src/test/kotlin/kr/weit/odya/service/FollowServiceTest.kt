package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getByFollowerIdAndFollowingIdIn
import kr.weit.odya.domain.follow.getByFollowingIdAndFollowerIdIn
import kr.weit.odya.domain.follow.getFollowerListBySearchCond
import kr.weit.odya.domain.follow.getFollowingListBySearchCond
import kr.weit.odya.domain.follow.getMayKnowFollowings
import kr.weit.odya.domain.follow.getVisitedFollowingIds
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.getByNickname
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_ANOTHER_USER_ID
import kr.weit.odya.support.TEST_DEFAULT_PAGEABLE
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_DEFAULT_SORT_TYPE
import kr.weit.odya.support.TEST_FOLLOWER_COUNT
import kr.weit.odya.support.TEST_FOLLOWING_COUNT
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PROFILE_URL
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createFollowCountsResponse
import kr.weit.odya.support.createFollowList
import kr.weit.odya.support.createFollowRequest
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createUser
import kr.weit.odya.support.createUsersDocument
import kr.weit.odya.support.createVisitedFollowingIds
import org.springframework.context.ApplicationEventPublisher

class FollowServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val followRepository = mockk<FollowRepository>()
        val fileService = mockk<FileService>()
        val usersDocumentRepository = mockk<UsersDocumentRepository>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val followService =
            FollowService(followRepository, userRepository, fileService, usersDocumentRepository, applicationEventPublisher)

        describe("createFollow") {
            context("이미 존재하지 않는 팔로우를 생성하는 경우") {
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                    )
                } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { userRepository.getByUserId(TEST_OTHER_USER_ID) } returns createOtherUser()
                every { followRepository.save(any()) } returns createFollow()
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny { followService.createFollow(TEST_USER_ID, createFollowRequest()) }
                }
            }

            context("이미 존재하는 팔로우인 경우") {
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                    )
                } returns true
                it("[ExistResourceException] 반환한다.") {
                    shouldThrow<ExistResourceException> {
                        followService.createFollow(
                            TEST_USER_ID,
                            createFollowRequest(),
                        )
                    }
                }
            }

            context("FOLLOWING ID의 사용자가 존재하지 않을 경우") {
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                    )
                } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { userRepository.getByUserId(TEST_OTHER_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다.") {
                    shouldThrow<NoSuchElementException> {
                        followService.createFollow(
                            TEST_USER_ID,
                            createFollowRequest(),
                        )
                    }
                }
            }
        }

        describe("deleteFollow") {
            context("FOLLOWER ID와 FOLLOWING ID가 주어지는 경우") {
                every { followRepository.deleteByFollowerIdAndFollowingId(TEST_USER_ID, TEST_OTHER_USER_ID) } just runs
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny { followService.deleteFollow(TEST_USER_ID, createFollowRequest()) }
                }
            }
        }

        describe("deleteFollower") {
            context("USERID와 FOLLOWER ID가 주어지는 경우") {
                every { followRepository.deleteByFollowerIdAndFollowingId(TEST_ANOTHER_USER_ID, TEST_USER_ID) } just runs
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny { followService.deleteFollower(TEST_USER_ID, TEST_ANOTHER_USER_ID) }
                }
            }
        }

        describe("getSliceFollowings") {
            context("FOLLOWER ID, PAGEABLE, SORT TYPE 정보가 주어지는 경우") {
                every {
                    followRepository.getFollowingListBySearchCond(
                        TEST_USER_ID,
                        TEST_DEFAULT_PAGEABLE,
                        TEST_DEFAULT_SORT_TYPE,
                    )
                } returns createFollowList()
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("SliceResponse<FollowUserResponse>를 반환한다.") {
                    val response =
                        followService.getSliceFollowings(TEST_USER_ID, TEST_USER_ID, TEST_DEFAULT_PAGEABLE, TEST_DEFAULT_SORT_TYPE)
                    response.content[0].userId shouldBe TEST_OTHER_USER_ID
                }
            }

            context("preAuthentication Access Url 생성에 실패한 경우") {
                every {
                    followRepository.getFollowingListBySearchCond(
                        TEST_USER_ID,
                        TEST_DEFAULT_PAGEABLE,
                        TEST_DEFAULT_SORT_TYPE,
                    )
                } returns createFollowList()
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                it("[ObjectStorageException] 반환한다.") {
                    shouldThrow<ObjectStorageException> {
                        followService.getSliceFollowings(TEST_USER_ID, TEST_USER_ID, TEST_DEFAULT_PAGEABLE, TEST_DEFAULT_SORT_TYPE)
                    }
                }
            }
        }

        describe("getSliceFollowers") {
            context("FOLLOWER ID, PAGEABLE, SORT TYPE 정보가 주어지는 경우") {
                every {
                    followRepository.getFollowerListBySearchCond(
                        TEST_USER_ID,
                        TEST_DEFAULT_PAGEABLE,
                        TEST_DEFAULT_SORT_TYPE,
                    )
                } returns createFollowList()
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("SliceResponse<FollowUserResponse>를 반환한다.") {
                    val response =
                        followService.getSliceFollowers(TEST_USER_ID, TEST_USER_ID, TEST_DEFAULT_PAGEABLE, TEST_DEFAULT_SORT_TYPE)
                    response.content[0].userId shouldBe TEST_USER_ID
                }
            }

            context("preAuthentication Access Url 생성에 실패한 경우") {
                every {
                    followRepository.getFollowerListBySearchCond(
                        TEST_USER_ID,
                        TEST_DEFAULT_PAGEABLE,
                        TEST_DEFAULT_SORT_TYPE,
                    )
                } returns createFollowList()
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다.") {
                    shouldThrow<ObjectStorageException> {
                        followService.getSliceFollowers(TEST_USER_ID, TEST_USER_ID, TEST_DEFAULT_PAGEABLE, TEST_DEFAULT_SORT_TYPE)
                    }
                }
            }
        }

        describe("getFollowCounts") {
            context("USER ID 정보가 주어지는 경우") {
                every { followRepository.countByFollowerId(TEST_USER_ID) } returns TEST_FOLLOWING_COUNT
                every { followRepository.countByFollowingId(TEST_USER_ID) } returns TEST_FOLLOWER_COUNT
                it("[FollowCountsResponse] 반환한다.") {
                    val response = followService.getFollowCounts(TEST_USER_ID)
                    response shouldBe createFollowCountsResponse()
                }
            }
        }

        describe("searchByFollowingNickname") {
            context("유효한 nickname이 주어지면") {
                val user = createUser()
                every { usersDocumentRepository.getByNickname(TEST_NICKNAME) } returns listOf(createUsersDocument(user))
                every {
                    followRepository.getByFollowerIdAndFollowingIdIn(
                        any(),
                        any(),
                        any(),
                        any(),
                    )
                } returns listOf(createFollow(following = user))
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_OTHER_USER_ID) } returns listOf(TEST_ANOTHER_USER_ID)
                it("유저를 조회 한다") {
                    shouldNotThrowAny { followService.searchByFollowingNickname(TEST_OTHER_USER_ID, TEST_USER_ID, TEST_NICKNAME, 10, null) }
                }
            }
        }

        describe("searchByFollowerNickname") {
            context("유효한 nickname이 주어지면") {
                val user = createUser()
                every { usersDocumentRepository.getByNickname(TEST_NICKNAME) } returns listOf(createUsersDocument(user))
                every { followRepository.getByFollowingIdAndFollowerIdIn(any(), any(), any(), any()) } returns listOf(createFollow(following = user))
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_OTHER_USER_ID) } returns listOf(TEST_ANOTHER_USER_ID)
                it("유저를 조회 한다") {
                    shouldNotThrowAny {
                        followService.searchByFollowerNickname(
                            TEST_OTHER_USER_ID,
                            TEST_USER_ID,
                            TEST_NICKNAME,
                            TEST_DEFAULT_SIZE,
                            null,
                        )
                    }
                }
            }
        }

        describe("getMayKnowFollowings") {
            context("요청이 들어오면") {
                every { followRepository.getMayKnowFollowings(any(), any(), any()) } returns listOf(createUser())
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("알수도 있는 유저를 조회 한다") {
                    shouldNotThrowAny { followService.getMayKnowFollowings(TEST_USER_ID, 10, null) }
                }
            }
        }

        describe("getVisitedFollowings") {
            context("요청이 들어오면") {
                every { followRepository.getVisitedFollowingIds(any(), any()) } returns createVisitedFollowingIds()
                every { userRepository.getByUserIds(any()) } returns listOf(createOtherUser(), createOtherUser())
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("방문한 유저를 조회 한다") {
                    shouldNotThrowAny { followService.getVisitedFollowings(TEST_PLACE_ID, TEST_USER_ID) }
                }
            }
        }
    },
)
