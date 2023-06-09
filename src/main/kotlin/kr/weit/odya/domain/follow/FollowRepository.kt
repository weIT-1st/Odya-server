package kr.weit.odya.domain.follow

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

fun FollowRepository.getFollowingListBySearchCond(
    followerId: Long,
    pageable: Pageable,
    sortType: FollowSortType,
): List<Follow> =
    findSliceByFollowerIdOrderBySortType(followerId, pageable, sortType)

fun FollowRepository.getFollowerListBySearchCond(
    followingId: Long,
    pageable: Pageable,
    sortType: FollowSortType,
): List<Follow> =
    findSliceByFollowingIdOrderBySortType(followingId, pageable, sortType)

interface FollowRepository : JpaRepository<Follow, Long>, CustomFollowRepository {
    fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean

    fun deleteByFollowerIdAndFollowingId(followerId: Long, followingId: Long)

    fun countByFollowerId(followerId: Long): Int

    fun countByFollowingId(followingId: Long): Int
}

interface CustomFollowRepository {
    fun findSliceByFollowerIdOrderBySortType(
        followerId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): List<Follow>

    fun findSliceByFollowingIdOrderBySortType(
        followingId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): List<Follow>
}

class FollowRepositoryImpl(private val queryFactory: QueryFactory) : CustomFollowRepository {
    override fun findSliceByFollowerIdOrderBySortType(
        followerId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): List<Follow> = queryFactory.listQuery {
        select(entity(Follow::class))
        from(entity(Follow::class))
        associate(Follow::class, User::class, on(Follow::follower))
        where(col(entity(User::class), User::id).equal(followerId))
        orderBy(dynamicOrderingByFollowSortType(sortType))
        offset(pageable.offset.toInt())
        limit(pageable.pageSize + 1)
    }

    override fun findSliceByFollowingIdOrderBySortType(
        followingId: Long,
        pageable: Pageable,
        sortType: FollowSortType,
    ): List<Follow> = queryFactory.listQuery {
        select(entity(Follow::class))
        from(entity(Follow::class))
        associate(Follow::class, entity(User::class), on(Follow::following))
        where(col(entity(User::class), User::id).equal(followingId))
        orderBy(dynamicOrderingByFollowSortType(sortType))
        offset(pageable.offset.toInt())
        limit(pageable.pageSize + 1)
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicOrderingByFollowSortType(
        sortType: FollowSortType,
    ): List<OrderSpec> =
        when (sortType) {
            FollowSortType.LATEST -> listOf(col(Follow::createdDate).desc())
            FollowSortType.OLDEST -> listOf(col(Follow::createdDate).asc())
        }
}

enum class FollowSortType(val description: String) {
    LATEST("최신순"), OLDEST("오래된순")
}
