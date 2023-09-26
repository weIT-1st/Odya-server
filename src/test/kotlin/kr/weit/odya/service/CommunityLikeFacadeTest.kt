package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_USER_ID
import org.redisson.api.RLock
import org.redisson.api.RedissonClient
import java.util.concurrent.TimeUnit

class CommunityLikeFacadeTest : DescribeSpec(
    {
        val communityLikeService = mockk<CommunityLikeService>()
        val redissonClient = mockk<RedissonClient>()
        val communityLikeFacade = CommunityLikeFacade(communityLikeService, redissonClient)
        val rLock = mockk<RLock>()
        describe("increaseLikeCount") {
            context("유효한 요청이 주어지는 경우") {
                every { redissonClient.getLock(TEST_COMMUNITY_ID.toString()) } returns rLock
                every { rLock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns true
                every { communityLikeService.increaseCommunityLikeCount(TEST_COMMUNITY_ID, TEST_USER_ID) } just runs
                every { rLock.unlock() } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityLikeFacade.increaseLikeCount(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("lock 획득에 실패하는 경우") {
                every { redissonClient.getLock(TEST_COMMUNITY_ID.toString()) } returns rLock
                every { rLock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns false
                it("[RedisLockFailedException] 반환한다") {
                    shouldThrow<RedisLockFailedException> {
                        communityLikeFacade.increaseLikeCount(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }
        }

        describe("decreaseLikeCount") {
            context("유효한 요청이 주어지는 경우") {
                every { redissonClient.getLock(TEST_COMMUNITY_ID.toString()) } returns rLock
                every { rLock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns true
                every { communityLikeService.decreaseCommunityLikeCount(TEST_COMMUNITY_ID, TEST_USER_ID) } just runs
                every { rLock.unlock() } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityLikeFacade.decreaseLikeCount(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("2번 lock 획득에 실패하는 경우") {
                every { redissonClient.getLock(TEST_COMMUNITY_ID.toString()) } returns rLock
                every { rLock.tryLock(any<Long>(), any<Long>(), any<TimeUnit>()) } returns false
                it("[RedisLockFailedException] 반환한다") {
                    shouldThrow<RedisLockFailedException> {
                        communityLikeFacade.decreaseLikeCount(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }
        }
    },
)
