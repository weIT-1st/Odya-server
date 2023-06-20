package kr.weit.odya.domain.follow

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FollowRepository : JpaRepository<Follow, Long> {
    fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean

    fun findPageByFollowerId(followerId: Long, pageable: Pageable): Page<Follow>

    fun findPageByFollowingId(followingId: Long, pageable: Pageable): Page<Follow>

    fun deleteByFollowerIdAndFollowingId(followerId: Long, followingId: Long)

    fun countByFollowerId(followerId: Long): Int

    fun countByFollowingId(followingId: Long): Int
}
