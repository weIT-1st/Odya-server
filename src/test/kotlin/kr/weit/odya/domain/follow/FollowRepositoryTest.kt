package kr.weit.odya.domain.follow

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_PAGEABLE
import kr.weit.odya.support.TEST_DEFAULT_SORT_TYPE
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class FollowRepositoryTest(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository
) : ExpectSpec(
    {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
        val follower: User = userRepository.save(createUser())
        val following: User = userRepository.save(createOtherUser())
        beforeEach {
            followRepository.save(createFollow(follower, following))
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
    },
)
