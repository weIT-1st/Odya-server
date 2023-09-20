package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.communitylike.CommunityLike
import kr.weit.odya.domain.communitylike.CommunityLikeId
import kr.weit.odya.domain.communitylike.CommunityLikeRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityLike
import kr.weit.odya.support.createUser

class CommunityLikeServiceTest : DescribeSpec(
    {
        val communityLikeRepository = mockk<CommunityLikeRepository>()
        val communityRepository = mockk<CommunityRepository>()
        val userRepository = mockk<UserRepository>()
        val communityLikeService = CommunityLikeService(communityLikeRepository, communityRepository, userRepository)

        describe("createCommunityLike") {
            context("유효한 요청이 주어지는 경우") {
                it("정상적으로 종료한다") {
                    val community = createCommunity()
                    val user = createUser()
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns community
                    every { userRepository.getByUserId(TEST_USER_ID) } returns user
                    every { communityLikeRepository.existsById(any<CommunityLikeId>()) } returns false
                    every { communityLikeRepository.save(any<CommunityLike>()) } returns createCommunityLike()

                    shouldNotThrowAny {
                        communityLikeService.createCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("존재하지 않는 커뮤니티 아이디가 주어지는 경우") {
                it("[NoSuchElementException] 반환한다") {
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                    shouldThrow<NoSuchElementException> {
                        communityLikeService.createCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("존재하지 않는 유저 아이디가 주어지는 경우") {
                it("[NoSuchElementException] 반환한다") {
                    every { userRepository.getByUserId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_USER_ID: 사용자가 존재하지 않습니다")
                    shouldThrow<NoSuchElementException> {
                        communityLikeService.createCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("요청 사용자가 이미 좋아요를 눌렀을 경우") {
                it("[ExistResourceException] 반환한다") {
                    val community = createCommunity()
                    val user = createUser()
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns community
                    every { userRepository.getByUserId(TEST_USER_ID) } returns user
                    every { communityLikeRepository.existsById(any<CommunityLikeId>()) } returns true
                    shouldThrow<ExistResourceException> {
                        communityLikeService.createCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }
        }

        describe("deleteCommunityLike") {
            context("유효한 요청이 주어지는 경우") {
                it("정상적으로 종료한다") {
                    val community = createCommunity()
                    val user = createUser()
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns community
                    every { userRepository.getByUserId(TEST_USER_ID) } returns user
                    every { communityLikeRepository.deleteById(any<CommunityLikeId>()) } just runs

                    shouldNotThrowAny {
                        communityLikeService.deleteCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("존재하지 않는 커뮤니티 아이디가 주어지는 경우") {
                it("[NoSuchElementException] 반환한다") {
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                    shouldThrow<NoSuchElementException> {
                        communityLikeService.deleteCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("존재하지 않는 유저 아이디가 주어지는 경우") {
                it("[NoSuchElementException] 반환한다") {
                    every { userRepository.getByUserId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_USER_ID: 사용자가 존재하지 않습니다")
                    shouldThrow<NoSuchElementException> {
                        communityLikeService.deleteCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }
        }
    },
)
