package kr.weit.odya.domain.traveljournal

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.expression.SubqueryExpressionSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import com.linecorp.kotlinjdsl.querydsl.from.Relation
import com.linecorp.kotlinjdsl.selectQuery
import com.linecorp.kotlinjdsl.subquery
import jakarta.ws.rs.ForbiddenException
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
    placeId: String?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findMyTravelJournalSliceBy(userId, size, lastId, placeId, sortType)

fun TravelJournalRepository.getFriendTravelJournalSliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    placeId: String?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findFriendTravelJournalSliceBy(userId, size, lastId, placeId, sortType)

fun TravelJournalRepository.getRecommendTravelJournalSliceBy(
    user: User,
    size: Int,
    lastId: Long?,
    placeId: String?,
    sortType: TravelJournalSortType,
): List<TravelJournal> = findRecommendTravelJournalSliceBy(user, size, lastId, placeId, sortType)

fun TravelJournalRepository.getTaggedTravelJournalSliceBy(
    user: User,
    size: Int,
    lastId: Long?,
): List<TravelJournal> = findTaggedTravelJournalSliceBy(user, size, lastId)

fun TravelJournalRepository.findTravelCompanionId(user: User, id: Long) =
    findByUserIdAndTravelJournalId(user, id) ?: throw ForbiddenException("요청 사용자(${user.id})는 해당 여행일지($id)의 같이 간 친구를 처리할 권한이 없습니다.")

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
        placeId: String?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>

    fun findFriendTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        placeId: String?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>

    fun findRecommendTravelJournalSliceBy(
        user: User,
        size: Int,
        lastId: Long?,
        placeId: String?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal>

    fun findTaggedTravelJournalSliceBy(
        user: User,
        size: Int,
        lastId: Long?,
    ): List<TravelJournal>

    fun findByUserIdAndTravelJournalId(user: User, id: Long): Long?
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
        getTravelJournalSliceBaseQuery(lastId, sortType, size, null)
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
        placeId: String?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        getTravelJournalSliceBaseQuery(lastId, sortType, size, placeId)
        where(nestedCol(col(TravelJournal::user), User::id).equal(userId))
    }

    override fun findFriendTravelJournalSliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        placeId: String?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        val followingIds = getFollowingIdsSubQuery(userId)
        getTravelJournalSliceBaseQuery(lastId, sortType, size, placeId)
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
        placeId: String?,
        sortType: TravelJournalSortType,
    ): List<TravelJournal> = queryFactory.listQuery {
        val sameAgeRangeFollowingIds = getSameAgeRangeFollowingIds(user)

        getTravelJournalSliceBaseQuery(lastId, sortType, size, placeId)
        where(
            and(
                col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                nestedCol(col(TravelJournal::user), User::id).`in`(sameAgeRangeFollowingIds),
            ),
        )
    }

    override fun findTaggedTravelJournalSliceBy(user: User, size: Int, lastId: Long?): List<TravelJournal> = queryFactory.listQuery {
        getTravelJournalSliceBaseQuery(lastId, TravelJournalSortType.LATEST, size, null)
        associate(
            entity(TravelJournal::class),
            entity(TravelCompanion::class),
            Relation<TravelJournal, TravelCompanion>("mutableTravelCompanions"),
        )
        where(
            and(
                col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                col(TravelCompanion::user).equal(user),
            ),
        )
    }

    override fun findByUserIdAndTravelJournalId(user: User, id: Long): Long? =
        queryFactory.selectQuery {
            select(col(TravelCompanion::id))
            from(entity(TravelJournal::class))
            associate(
                entity(TravelJournal::class),
                entity(TravelCompanion::class),
                Relation<TravelJournal, TravelCompanion>("mutableTravelCompanions"),
            )
            associate(
                entity(TravelJournal::class),
                entity(TravelJournalInformation::class),
                on(TravelJournal::travelJournalInformation),
            )
            where(
                and(
                    col(TravelJournalInformation::visibility).notEqual(TravelJournalVisibility.PRIVATE),
                    col(TravelCompanion::user).equal(user),
                    col(TravelJournal::id).equal(id),
                ),
            )
        }.resultList.firstOrNull()

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
        placeId: String?,
    ) {
        select(entity(TravelJournal::class))
        from(entity(TravelJournal::class))
        associate(
            entity(TravelJournal::class),
            entity(TravelJournalInformation::class),
            on(TravelJournal::travelJournalInformation),
        )
        where(dynamicPredicateByLastId(lastId, sortType))
        if (placeId != null) {
            associate(
                TravelJournal::class,
                entity(TravelJournalContent::class),
                Relation<TravelJournal, TravelJournalContent>("mutableTravelJournalContents"),
            )
            where(nestedCol(col(TravelJournalContent::travelJournalContentInformation), TravelJournalContentInformation::placeId).equal(placeId))
        }
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
