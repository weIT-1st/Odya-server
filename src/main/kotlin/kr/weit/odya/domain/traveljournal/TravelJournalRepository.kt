package kr.weit.odya.domain.traveljournal

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import com.linecorp.kotlinjdsl.querydsl.from.Relation
import com.linecorp.kotlinjdsl.subquery
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun TravelJournalRepository.getByUserId(userId: Long): List<TravelJournal> = findAllByUserId(userId)

fun TravelJournalRepository.getByTravelJournalId(travelJournalId: Long): TravelJournal =
    findByIdOrNull(travelJournalId) ?: throw NoSuchElementException("$travelJournalId : 해당 여행 일지가 존재하지 않습니다.")

fun TravelJournalRepository.getByContentImageNames(travelJournalId: Long): List<String> =
    findContentImageNameListById(travelJournalId)

fun TravelJournalRepository.getMyTravelJournalSliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findMyTravelJournalSliceBy(userId, size, lastId, sortType)

fun TravelJournalRepository.getFriendTravelJournalSliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findFriendTravelJournalSliceBy(userId, size, lastId, sortType)

fun TravelJournalRepository.getRecommendTravelJournalSliceBy(
    user: User,
    size: Int,
    lastId: Long?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findRecommendTravelJournalSliceBy(user, size, lastId, sortType)

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long>, CustomTravelJournalRepository {
    fun findAllByUserId(userId: Long): List<TravelJournal>

    fun deleteAllByUserId(userId: Long)
}

interface CustomTravelJournalRepository {
    fun findContentImageNameListById(
        travelJournalId: Long,
    ): List<String>

    fun findMyTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>

    fun findFriendTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>

    fun findRecommendTravelJournalSliceBy(
        user: User,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>
}

class CustomTravelJournalRepositoryImpl(private val queryFactory: QueryFactory) : CustomTravelJournalRepository {
    override fun findContentImageNameListById(
        travelJournalId: Long,
    ): List<String> = queryFactory.listQuery {
        select(col(ContentImage::name))
        from(entity(TravelJournal::class))
        associate(
            TravelJournal::class,
            entity(TravelJournalContent::class),
            Relation<TravelJournal, TravelJournalContent>("mutableTravelJournalContents"),
        )
        associate(
            TravelJournalContent::class,
            entity(TravelJournalContentImage::class),
            Relation<TravelJournalContent, TravelJournalContentImage>("mutableTravelJournalContentImages"),
        )
        associate(TravelJournalContentImage::class, ContentImage::class, on(TravelJournalContentImage::contentImage))
        where(col(TravelJournal::id).equal(travelJournalId))
    }

    override fun findMyTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        select(entity(TravelJournal::class))
        from(entity(TravelJournal::class))
        where(
            and(
                nestedCol(col(TravelJournal::user), User::id).equal(userId),
                dynamicPredicateByLastId(lastId, sortType),
            ),
        )
        orderBy(dynamicOrderingSortType(sortType))
        limit(size + 1)
    }

    override fun findFriendTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        val followingIds = queryFactory.subquery<Long> {
            select(nestedCol(col(Follow::following), User::id))
            from(entity(Follow::class))
            where(nestedCol(col(Follow::follower), User::id).equal(userId))
        }

        select(entity(TravelJournal::class))
        from(entity(TravelJournal::class))
        associate(
            entity(TravelJournal::class),
            entity(TravelJournalInformation::class),
            on(TravelJournal::travelJournalInformation),
        )
        where(
            and(
                col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                nestedCol(col(TravelJournal::user), User::id).`in`(followingIds),
                dynamicPredicateByLastId(lastId, sortType),
            ),
        )
        orderBy(dynamicOrderingSortType(sortType))
        limit(size + 1)
    }

    override fun findRecommendTravelJournalSliceBy(
        user: User,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        val sameAgeRangeFollowingIds = getSameAgeRangeFollowingIds(user)

        select(entity(TravelJournal::class))
        from(entity(TravelJournal::class))
        associate(
            entity(TravelJournal::class),
            entity(TravelJournalInformation::class),
            on(TravelJournal::travelJournalInformation),
        )
        where(
            and(
                col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                nestedCol(col(TravelJournal::user), User::id).`in`(sameAgeRangeFollowingIds),
                dynamicPredicateByLastId(lastId, sortType),
            ),
        )
        orderBy(dynamicOrderingSortType(sortType))
        limit(size + 1)
    }

    private fun getSameAgeRangeFollowingIds(user: User): List<Long> {
        val userAgeRange = user.getAgeRange()
        val followingIds = queryFactory.listQuery<User> {
            select(col(entity(Follow::class), Follow::following))
            from(entity(Follow::class))
            where(nestedCol(col(Follow::follower), User::id).equal(user.id))
        }
            .filter { it.getAgeRange() == userAgeRange }
            .map { it.id }
        return followingIds
    }

    private fun CriteriaQueryDsl<TravelJournal>.dynamicPredicateByLastId(
        lastId: Long?,
        sortType: TravelJournalSortType,
    ) = if (lastId != null) {
        when (sortType) {
            TravelJournalSortType.LATEST -> col(TravelJournal::id).lessThan(lastId)
        }
    } else {
        PredicateSpec.empty
    }

    private fun CriteriaQueryDsl<TravelJournal>.dynamicOrderingSortType(
        sortType: TravelJournalSortType,
    ): List<OrderSpec> =
        when (sortType) {
            TravelJournalSortType.LATEST -> listOf(col(TravelJournal::id).desc())
        }
}

enum class TravelJournalSortType(private val description: String) {
    LATEST("최신순"),
}
