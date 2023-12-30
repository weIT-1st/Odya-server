package kr.weit.odya.domain.traveljournalbookmark

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import com.linecorp.kotlinjdsl.subquery
import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalInformation
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun TravelJournalBookmarkRepository.getSliceBy(
    size: Int,
    lastId: Long?,
    sortType: TravelJournalBookmarkSortType,
    user: User,
): List<TravelJournalBookmark> = findSliceBy(size, lastId, sortType, user)

fun TravelJournalBookmarkRepository.getSliceByOther(
    size: Int,
    lastId: Long?,
    sortType: TravelJournalBookmarkSortType,
    user: User,
    loginUserId: Long,
): List<TravelJournalBookmark> = findSliceByOther(size, lastId, sortType, user, loginUserId)

fun TravelJournalBookmarkRepository.getTravelJournalIds(userId: Long): List<Long> = findTravelJournalIdsByUserId(userId)

@Repository
interface TravelJournalBookmarkRepository :
    JpaRepository<TravelJournalBookmark, Long>,
    CustomTravelJournalBookmarkRepository {
    fun existsByUserAndTravelJournal(user: User, travelJournal: TravelJournal): Boolean

    fun existsByUserIdAndTravelJournal(userId: Long, travelJournal: TravelJournal): Boolean

    fun deleteByUserAndTravelJournal(user: User, travelJournal: TravelJournal)

    fun deleteAllByTravelJournalId(travelJournalId: Long)

    fun deleteAllByUserId(userId: Long)
}

interface CustomTravelJournalBookmarkRepository {
    fun findSliceBy(
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
        user: User,
    ): List<TravelJournalBookmark>

    fun findSliceByOther(
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
        user: User,
        loginUserId: Long,
    ): List<TravelJournalBookmark>

    fun findTravelJournalIdsByUserId(userId: Long): List<Long>
}

class CustomTravelJournalBookmarkRepositoryImpl(
    private val queryFactory: QueryFactory,
) : CustomTravelJournalBookmarkRepository {
    override fun findSliceBy(
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
        user: User,
    ): List<TravelJournalBookmark> = queryFactory.listQuery {
        getTravelJournalBookmarkSliceBaseQuery(size, lastId, sortType)
        where(
            col(TravelJournalBookmark::user).equal(user),
        )
    }

    override fun findSliceByOther(
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
        user: User,
        loginUserId: Long,
    ): List<TravelJournalBookmark> = queryFactory.listQuery {
        val followingIds = getFollowingsSubQuery(loginUserId)
        getTravelJournalBookmarkSliceBaseQuery(size, lastId, sortType)
        associate(entity(TravelJournalBookmark::class), entity(TravelJournal::class), on(TravelJournalBookmark::travelJournal))
        where(
            and(
                col(TravelJournalBookmark::user).equal(user),
                or(
                    nestedCol(col(TravelJournal::travelJournalInformation), TravelJournalInformation::visibility).equal(TravelJournalVisibility.PUBLIC),
                    and(
                        nestedCol(col(TravelJournal::travelJournalInformation), TravelJournalInformation::visibility).equal(TravelJournalVisibility.FRIEND_ONLY),
                        nestedCol(col(TravelJournal::user), User::id).`in`(followingIds),
                    ),
                ),
            ),
        )
    }

    override fun findTravelJournalIdsByUserId(userId: Long): List<Long> =
        queryFactory.listQuery {
            select(nestedCol(col(TravelJournalBookmark::travelJournal), TravelJournal::id))
            from(entity(TravelJournalBookmark::class))
            where(nestedCol(col(TravelJournalBookmark::user), User::id).equal(userId))
        }

    private fun CriteriaQueryDsl<TravelJournalBookmark>.getTravelJournalBookmarkSliceBaseQuery(
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
    ) {
        select(TravelJournalBookmark::class.java)
        from(TravelJournalBookmark::class.java)
        where(
            dynamicPredicateByLastId(lastId, sortType),
        )
        orderBy(dynamicOrderingSortType(sortType))
        limit(size + 1)
    }

    private fun CriteriaQueryDsl<TravelJournalBookmark>.dynamicPredicateByLastId(
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
    ) = if (lastId != null) {
        when (sortType) {
            TravelJournalBookmarkSortType.LATEST -> col(TravelJournalBookmark::id).lessThan(lastId)
        }
    } else {
        PredicateSpec.empty
    }

    private fun CriteriaQueryDsl<TravelJournalBookmark>.dynamicOrderingSortType(sortType: TravelJournalBookmarkSortType): List<OrderSpec> =
        when (sortType) {
            TravelJournalBookmarkSortType.LATEST -> listOf(col(TravelJournalBookmark::id).desc())
        }

    private fun getFollowingsSubQuery(userId: Long) = queryFactory.subquery<Long> {
        select(nestedCol(col(Follow::following), User::id))
        from(entity(Follow::class))
        where(nestedCol(col(Follow::follower), User::id).equal(userId))
    }
}

enum class TravelJournalBookmarkSortType(private val description: String) {
    LATEST("최신순"),
}
