package kr.weit.odya.domain.follow

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import com.linecorp.kotlinjdsl.querydsl.from.Relation
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityInformation
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentInformation
import kr.weit.odya.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

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

fun FollowRepository.getByFollowingIdAndFollowerIdIn(
    following: Long,
    followerIds: List<Long>,
    size: Int,
    lastId: Long?,
): List<Follow> =
    findAllByFollowingIdAndFollowerIdInAndLastId(following, followerIds, size, lastId)

fun FollowRepository.getMayKnowFollowings(
    followerId: Long,
    size: Int,
    lastId: Long?,
): List<Follow> =
    findMayKnowFollowings(followerId, size, lastId)

fun FollowRepository.getFollowerFcmTokens(followingId: Long): List<String> =
    findFollowerFcmTokenByFollowingId(followingId).filterNotNull()

fun FollowRepository.getVisitedFollowingIds(placeID: String, followerId: Long): List<Long> =
    findVisitedFollowingIdsByPlaceIdAndFollowerId(placeID, followerId)

fun FollowRepository.getFollowingIds(followerId: Long): List<Long> =
    findFollowingIdsByFollowerId(followerId)

interface FollowRepository : JpaRepository<Follow, Long>, CustomFollowRepository {
    fun existsByFollowerIdAndFollowingId(followerId: Long, followingId: Long): Boolean

    fun deleteByFollowerIdAndFollowingId(followerId: Long, followingId: Long)

    fun countByFollowerId(followerId: Long): Int

    fun countByFollowingId(followingId: Long): Int

    fun deleteByFollowingId(followingId: Long)

    fun deleteByFollowerId(follower: Long)

    @Query("select f.following.id from Follow f where f.follower.id = :followerId")
    fun findFollowingIdsByFollowerId(followerId: Long): List<Long>
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

    fun findAllByFollowingIdAndFollowerIdInAndLastId(
        following: Long,
        followerIds: List<Long>,
        size: Int,
        lastId: Long?,
    ): List<Follow>

    fun findMayKnowFollowings(
        followerId: Long,
        size: Int,
        lastId: Long?,
    ): List<Follow>

    fun findFollowerFcmTokenByFollowingId(followingId: Long): List<String?>

    fun findVisitedFollowingIdsByPlaceIdAndFollowerId(placeID: String, followerId: Long): List<Long>
}

open class FollowRepositoryImpl(private val queryFactory: QueryFactory) : CustomFollowRepository {
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
            where(col(followingUser, User::id).greaterThan(lastId))
        }
        limit(size)
    }

    override fun findAllByFollowingIdAndFollowerIdInAndLastId(
        followingId: Long,
        followerIds: List<Long>,
        size: Int,
        lastId: Long?,
    ): List<Follow> = queryFactory.listQuery {
        select(entity(Follow::class))
        from(entity(Follow::class))
        where(
            and(
                nestedCol(col(Follow::following), User::id).equal(followingId),
                nestedCol(col(Follow::follower), User::id).`in`(followerIds),
            ),
        )
        if (lastId != null) {
            where(nestedCol(col(Follow::follower), User::id).greaterThan(lastId))
        }
        limit(size)
    }

    @Transactional(readOnly = true)
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

    override fun findFollowerFcmTokenByFollowingId(followingId: Long): List<String?> = queryFactory.listQuery {
        val followerUser = entity(User::class, alias = "followerUser")
        val followingUser = entity(User::class, alias = "followingUser")
        select(column(followerUser, User::fcmToken))
        from(entity(Follow::class))
        associate(Follow::class, followerUser, on(Follow::follower))
        associate(Follow::class, followingUser, on(Follow::following))
        where(col(followingUser, User::id).equal(followingId))
    }

    override fun findVisitedFollowingIdsByPlaceIdAndFollowerId(placeID: String, followerId: Long): List<Long> {
        val placeReviewWriter = queryFactory.listQuery {
            select(col(User::id))
            from(entity(PlaceReview::class))
            associate(PlaceReview::class, entity(User::class), on(PlaceReview::user))
            where(col(PlaceReview::placeId).equal(placeID))
        }
        val travelJournalWriter = queryFactory.listQuery {
            select(col(User::id))
            from(entity(TravelJournal::class))
            associate(
                TravelJournal::class,
                entity(TravelJournalContent::class),
                Relation<TravelJournal, TravelJournalContent>("mutableTravelJournalContents"),
            )
            associate(
                TravelJournalContent::class,
                entity(TravelJournalContentInformation::class),
                on(TravelJournalContent::travelJournalContentInformation),
            )
            associate(TravelJournal::class, entity(User::class), on(TravelJournal::user))
            where(col(TravelJournalContentInformation::placeId).equal(placeID))
        }
        val communityWriter = queryFactory.listQuery {
            select(col(User::id))
            from(entity(Community::class))
            associate(Community::class, entity(User::class), on(Community::user))
            associate(Community::class, entity(CommunityInformation::class), on(Community::communityInformation))
            where(col(CommunityInformation::placeId).equal(placeID))
        }

        // JPA는 유니온을 지원하지 않기 때문에 아래와 같이 작업을 했다
        val userList = (placeReviewWriter + travelJournalWriter + communityWriter).distinct()

        return queryFactory.listQuery {
            val followerUser = entity(User::class, alias = "followerUser")
            val followingUser = entity(User::class, alias = "followingUser")
            select(col(followingUser, User::id))
            from(entity(Follow::class))
            associate(Follow::class, followerUser, on(Follow::follower))
            associate(Follow::class, followingUser, on(Follow::following))
            where(
                and(
                    col(followerUser, User::id).equal(followerId),
                    col(followingUser, User::id).`in`(userList),
                ),
            )
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
