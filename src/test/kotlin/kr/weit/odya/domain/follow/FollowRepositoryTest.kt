package kr.weit.odya.domain.follow

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_PAGEABLE
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_DEFAULT_SORT_TYPE
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class FollowRepositoryTest(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        val follower: User = userRepository.save(createUser())
        val following: User = userRepository.save(createOtherUser())
        val notFollowing: User = userRepository.save(createCustomUser("test_user_3", "test_user_3"))
        beforeEach {
            followRepository.save(createFollow(follower, following))
            followRepository.save(createFollow(following, follower))
            followRepository.save(createFollow(following, notFollowing))
        }

        context("팔로우 목록 조회") {
            expect("FOLLOWER가 팔로잉하는 사용자 목록을 조회한다") {
                val result = followRepository.getFollowingListBySearchCond(
                    follower.id,
                    TEST_DEFAULT_PAGEABLE,
                    TEST_DEFAULT_SORT_TYPE,
                )
                result.size shouldBe 1
            }

            expect("FOLLOWING을 팔로워하는 사용자 목록을 조회한다") {
                val result = followRepository.getFollowerListBySearchCond(
                    following.id,
                    TEST_DEFAULT_PAGEABLE,
                    TEST_DEFAULT_SORT_TYPE,
                )
                result.size shouldBe 1
            }
        }

        context("팔로우 여부 확인") {
            expect("FOLLOWER ID와 FOLLOWING ID가 일치하는 팔로우 여부를 확인한다") {
                val result = followRepository.existsByFollowerIdAndFollowingId(follower.id, following.id)
                result shouldBe true
            }
        }

        context("팔로우 수 확인") {
            expect("FOLLOWER가 팔로잉하는 사용자의 수를 조회한다") {
                val result = followRepository.countByFollowerId(follower.id)
                result shouldBe 1
            }

            expect("FOLLOWING을 팔로워하는 사용자의 수를 조회한다") {
                val result = followRepository.countByFollowingId(following.id)
                result shouldBe 1
            }
        }

        context("팔로우 삭제") {
            expect("FOLLOWER ID와 FOLLOWING ID가 일치하는 팔로우를 삭제한다") {
                followRepository.deleteByFollowerIdAndFollowingId(follower.id, following.id)
                followRepository.existsByFollowerIdAndFollowingId(follower.id, following.id) shouldBe false
            }
        }

        context("FOLLOWER가 팔로잉하는 사용자 목록을 전체 삭제한다") {
            expect("userId와 일치하는 팔로잉 목록을 전체 삭제한다") {
                followRepository.deleteByFollowerId(follower.id)
                followRepository.countByFollowerId(follower.id) shouldBe 0
            }
        }

        context("FOLLOWING이 팔로워하는 사용자 목록을 전체 삭제한다") {
            expect("userId와 일치하는 팔로우 목록을 전체 삭제한다") {
                followRepository.deleteByFollowingId(following.id)
                followRepository.countByFollowingId(following.id) shouldBe 0
            }
        }

        context("팔로잉 유저 id list 검색") {
            expect("팔로잉 id list에 해당하는 팔로잉 유저를 조회한다") {
                val userIds = listOf(following.id, following.id + 1)
                val result = followRepository.getByFollowerIdAndFollowingIdIn(
                    follower.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    null,
                )
                result.size shouldBe 1
            }

            expect("팔로잉 id list에 해당하는 팔로잉 유저를 size만큼 조회한다") {
                val userIds = listOf(following.id, following.id + 1)
                val result =
                    followRepository.getByFollowerIdAndFollowingIdIn(follower.id, userIds, 1, null)
                result.size shouldBe 1
            }

            expect("팔로잉 id list에 해당하는 lastId보다 작은 팔로잉 유저를 조회한다") {
                val userIds = listOf(following.id, following.id + 1)
                var result = followRepository.getByFollowerIdAndFollowingIdIn(
                    follower.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    following.id + 1,
                )
                result.size shouldBe 1
                result = followRepository.getByFollowerIdAndFollowingIdIn(
                    follower.id,
                    userIds,
                    TEST_DEFAULT_SIZE,
                    following.id,
                )
                result.size shouldBe 0
            }
        }

        context("알수도 있는 유저 검색") {
            expect("알수도 있는 유저를 조회한다") {
                val result = followRepository.getMayKnowFollowings(
                    follower.id,
                    TEST_DEFAULT_SIZE,
                    null,
                )
                result.size shouldBe 1
            }

            expect("lastId보다 작은 알수도 있는 유저를 조회한다") {
                var result = followRepository.getMayKnowFollowings(
                    follower.id,
                    TEST_DEFAULT_SIZE,
                    notFollowing.id + 1,
                )
                result.size shouldBe 1

                result = followRepository.getMayKnowFollowings(
                    follower.id,
                    TEST_DEFAULT_SIZE,
                    notFollowing.id,
                )
                result.size shouldBe 0
            }
        }
    },
)
