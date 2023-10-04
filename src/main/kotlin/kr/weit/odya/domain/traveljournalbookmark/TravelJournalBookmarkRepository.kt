package kr.weit.odya.domain.traveljournalbookmark

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun TravelJournalBookmarkRepository.getSliceBy(
    size: Int,
    lastId: Long?,
    sortType: TravelJournalBookmarkSortType,
    user: User,
): List<TravelJournalBookmark> = findSliceBy(size, lastId, sortType, user)

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
}

enum class TravelJournalBookmarkSortType(private val description: String) {
    LATEST("최신순"),
}
