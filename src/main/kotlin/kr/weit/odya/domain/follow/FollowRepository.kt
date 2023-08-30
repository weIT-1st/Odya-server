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

fun FollowRepository.getByFollowerIdAndFollowingIdIn(
    follower: Long,
    followingIds: List<Long>,
    size: Int,
    lastId: Long?,
): List<Follow> =
    findAllByFollowerIdAndFollowingIdInAndLastId(follower, followingIds, size, lastId)

fun FollowRepository.getMayKnowFollowings(
    followerId: Long,
    size: Int,
    lastId: Long?,
): List<Follow> =
    findMayKnowFollowings(followerId, size, lastId)

interface FollowRepository : JpaRepository<Follow, Long>, CustomFollowRepository {
    fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean

    fun deleteByFollowerIdAndFollowingId(followerId: Long, followingId: Long)

    fun countByFollowerId(followerId: Long): Int

    fun countByFollowingId(followingId: Long): Int

    fun deleteByFollowingId(followingId: Long)

    fun deleteByFollowerId(follower: Long)
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

    fun findAllByFollowerIdAndFollowingIdInAndLastId(
        followerId: Long,
        followingIds: List<Long>,
        size: Int,
        lastId: Long?,
    ): List<Follow>

    fun findMayKnowFollowings(
        followerId: Long,
        size: Int,
        lastId: Long?,
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

    override fun findAllByFollowerIdAndFollowingIdInAndLastId(
        followerId: Long,
        followingIds: List<Long>,
        size: Int,
        lastId: Long?,
    ): List<Follow> = queryFactory.listQuery {
        val followerUser = entity(User::class, alias = "followerUser")
        val followingUser = entity(User::class, alias = "followingUser")
        select(entity(Follow::class))
        from(entity(Follow::class))
        associate(Follow::class, followerUser, on(Follow::follower))
        associate(Follow::class, followingUser, on(Follow::following))
        where(
            and(
                col(followerUser, User::id).equal(followerId),
                col(followingUser, User::id).`in`(followingIds),
            ),
        )
        if (lastId != null) {
            where(col(followingUser, User::id).lessThan(lastId))
        }
        limit(size)
    }

    override fun findMayKnowFollowings(
        followerId: Long,
        size: Int,
        lastId: Long?,
    ): List<Follow> {
        val followingList = queryFactory.listQuery<User> {
            select(column(entity(Follow::class), Follow::following))
            from(entity(Follow::class))
            associate(entity(Follow::class), entity(User::class), on(Follow::follower))
            where(col(entity(User::class), User::id).equal(followerId))
        }

        return queryFactory.listQuery {
            select(entity(Follow::class))
            from(entity(Follow::class))
            associate(Follow::class, entity(User::class), on(Follow::following))
            where(
                and(
                    col(entity(User::class), User::id).notEqual(followerId),
                    col(entity(Follow::class), Follow::follower).`in`(followingList),
                    not(col(entity(Follow::class), Follow::following).`in`(followingList)),
                ),
            )
            if (lastId != null) {
                where(col(entity(User::class), User::id).lessThan(lastId))
            }
            limit(size)
        }
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
