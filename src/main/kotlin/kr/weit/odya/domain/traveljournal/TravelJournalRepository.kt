package kr.weit.odya.domain.traveljournal

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.expression.SubqueryExpressionSpec
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

fun TravelJournalRepository.getTravelJournalSliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findTravelJournalSliceBy(userId, size, lastId, sortType)

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

    fun findTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>

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

    override fun findTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        getTravelJournalSliceBaseQuery(lastId, sortType, size)
        val followingIds = getFollowingIdsSubQuery(userId)
        val publicTravelJournalIds = getPublicTravelJournalIdsSubQuery()
        val friendOnlyTravelJournalIds = getFriendOnlyTravelJournalIdsSubQuery(followingIds)
        val myFriendOnlyTravelJournalIds = getMyFriendOnlyTravelJournalIdsSubQuery(userId)
        where(
            or(
                col(TravelJournal::id).`in`(publicTravelJournalIds),
                col(TravelJournal::id).`in`(friendOnlyTravelJournalIds),
                col(TravelJournal::id).`in`(myFriendOnlyTravelJournalIds),
            ),
        )
    }

    override fun findMyTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        getTravelJournalSliceBaseQuery(lastId, sortType, size)
        where(nestedCol(col(TravelJournal::user), User::id).equal(userId))
    }

    override fun findFriendTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        val followingIds = getFollowingIdsSubQuery(userId)
        getTravelJournalSliceBaseQuery(lastId, sortType, size)
        where(
            and(
                col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                nestedCol(col(TravelJournal::user), User::id).`in`(followingIds),
            ),
        )
    }

    override fun findRecommendTravelJournalSliceBy(
        user: User,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        val sameAgeRangeFollowingIds = getSameAgeRangeFollowingIds(user)

        getTravelJournalSliceBaseQuery(lastId, sortType, size)
        where(
            and(
                col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                nestedCol(col(TravelJournal::user), User::id).`in`(sameAgeRangeFollowingIds),
            ),
        )
    }

    private fun getMyFriendOnlyTravelJournalIdsSubQuery(userId: Long) =
        queryFactory.subquery<Long> {
            val tj3Entity = entity(TravelJournal::class, "tj3")
            val tji3Entity = entity(TravelJournalInformation::class, "tji3")
            associate(
                tj3Entity,
                tji3Entity,
                on(TravelJournal::travelJournalInformation),
            )
            select(col(tj3Entity, TravelJournal::id))
            from(tj3Entity)
            where(
                and(
                    col(tji3Entity, TravelJournalInformation::visibility).equal(TravelJournalVisibility.FRIEND_ONLY),
                    nestedCol(col(tj3Entity, TravelJournal::user), User::id).equal(userId),
                ),
            )
        }

    private fun getFriendOnlyTravelJournalIdsSubQuery(followingIds: SubqueryExpressionSpec<Long>) =
        queryFactory.subquery<Long> {
            val tj2Entity = entity(TravelJournal::class, "tj2")
            val tji2Entity = entity(TravelJournalInformation::class, "tji2")
            associate(
                tj2Entity,
                tji2Entity,
                on(TravelJournal::travelJournalInformation),
            )
            select(col(tj2Entity, TravelJournal::id))
            from(tj2Entity)
            where(
                and(
                    col(tji2Entity, TravelJournalInformation::visibility).equal(TravelJournalVisibility.FRIEND_ONLY),
                    nestedCol(col(tj2Entity, TravelJournal::user), User::id).`in`(followingIds),
                ),
            )
        }

    private fun getPublicTravelJournalIdsSubQuery() = queryFactory.subquery<Long> {
        val tj1Entity = entity(TravelJournal::class, "tj1")
        val tji1Entity = entity(TravelJournalInformation::class, "tji1")
        associate(
            tj1Entity,
            tji1Entity,
            on(TravelJournal::travelJournalInformation),
        )
        select(col(tj1Entity, TravelJournal::id))
        from(tj1Entity)
        where(col(tji1Entity, TravelJournalInformation::visibility).equal(TravelJournalVisibility.PUBLIC))
    }

    private fun getFollowingIdsSubQuery(userId: Long) = queryFactory.subquery<Long> {
        select(nestedCol(col(Follow::following), User::id))
        from(entity(Follow::class))
        where(nestedCol(col(Follow::follower), User::id).equal(userId))
    }

    private fun CriteriaQueryDsl<TravelJournal>.getTravelJournalSliceBaseQuery(
        lastId: Long?,
        sortType: TravelJournalSortType,
        size: Int,
    ) {
        select(entity(TravelJournal::class))
        from(entity(TravelJournal::class))
        associate(
            entity(TravelJournal::class),
            entity(TravelJournalInformation::class),
            on(TravelJournal::travelJournalInformation),
        )
        where(dynamicPredicateByLastId(lastId, sortType))
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

    private fun CriteriaQueryDsl<TravelJournal>.dynamicOrderingSortType(sortType: TravelJournalSortType): List<OrderSpec> =
        when (sortType) {
            TravelJournalSortType.LATEST -> listOf(col(TravelJournal::id).desc())
        }
}

enum class TravelJournalSortType(private val description: String) {
    LATEST("최신순"),
}
