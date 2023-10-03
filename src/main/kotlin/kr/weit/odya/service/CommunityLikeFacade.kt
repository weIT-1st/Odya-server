package kr.weit.odya.service

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private const val DEFAULT_COMMUNITY_LIKE_COUNT_LOCK_WAIT_TIME_MS = 10000L
private const val DEFAULT_COMMUNITY_LIKE_COUNT_LOCK_REASE_TIME_MS = 3000L

@Component
class CommunityLikeFacade(
    private val communityLikeService: CommunityLikeService,
    private val redissonClient: RedissonClient,
) {
    fun increaseLikeCount(communityId: Long, userId: Long) {
        executeWithCommunityRedisLock(communityId) {
            communityLikeService.increaseCommunityLikeCount(communityId, userId)
        }
    }

    fun decreaseLikeCount(communityId: Long, userId: Long) {
        executeWithCommunityRedisLock(communityId) {
            communityLikeService.decreaseCommunityLikeCount(communityId, userId)
        }
    }

    private fun executeWithCommunityRedisLock(communityId: Long, execute: () -> Unit) {
        val lock = redissonClient.getLock(generateRedisCommunityLockKey(communityId))
        val available = lock.tryLock(
            DEFAULT_COMMUNITY_LIKE_COUNT_LOCK_WAIT_TIME_MS,
            DEFAULT_COMMUNITY_LIKE_COUNT_LOCK_REASE_TIME_MS,
            TimeUnit.MILLISECONDS,
        ) // Lock을 얻기 위해 10초 대기, Lock은 3초 동안 유지됨

        try {
            if (!available) {
                throw RedisLockFailedException("너무 많은 요청으로 인해 커뮤니티($communityId) 좋아요 요청에 실패했습니다. 잠시 후 다시 시도해주세요")
            }
            execute()
        } finally {
            if (available) {
                lock.unlock()
            }
        }
    }

    private fun generateRedisCommunityLockKey(communityId: Long) = communityId.toString()
}
