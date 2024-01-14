package kr.weit.odya.domain.representativetraveljournal

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
import org.springframework.data.jpa.repository.Query

fun RepresentativeTravelJournalRepository.getSliceBy(
    size: Int,
    lastId: Long?,
    sortType: RepresentativeTravelJournalSortType,
    user: User,
): List<RepresentativeTravelJournal> = findSliceBy(size, lastId, sortType, user)

fun RepresentativeTravelJournalRepository.getTargetSliceBy(
    size: Int,
    lastId: Long?,
    sortType: RepresentativeTravelJournalSortType,
    targetUser: User,
    loginUserId: Long,
): List<RepresentativeTravelJournal> = findTargetSliceBy(size, lastId, sortType, targetUser, loginUserId)

fun RepresentativeTravelJournalRepository.getRepTravelJournalIds(userId: Long): List<Long> =
    findTravelJournalIdsByUserId(userId)

interface RepresentativeTravelJournalRepository :
    JpaRepository<RepresentativeTravelJournal, Long>,
    CustomRepresentativeTravelJournalRepository {
    fun existsByUserAndTravelJournal(user: User, travelJournal: TravelJournal): Boolean

    fun deleteByUserAndTravelJournal(user: User, travelJournal: TravelJournal)

    fun existsByUserIdAndTravelJournal(userId: Long, travelJournal: TravelJournal): Boolean

    @Query("select rtj.travelJournal.id from RepresentativeTravelJournal rtj where rtj.user.id = :userId")
    fun findTravelJournalIdsByUserId(userId: Long): List<Long>
}

interface CustomRepresentativeTravelJournalRepository {
    fun findSliceBy(
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
        user: User,
    ): List<RepresentativeTravelJournal>

    fun findTargetSliceBy(
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
        targetUser: User,
        loginUserId: Long,
    ): List<RepresentativeTravelJournal>
}

class CustomRepresentativeTravelJournalRepositoryImpl(
    private val queryFactory: QueryFactory,
) : CustomRepresentativeTravelJournalRepository {
    override fun findSliceBy(
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
        user: User,
    ): List<RepresentativeTravelJournal> = queryFactory.listQuery {
        getRepTravelJournalSliceBaseQuery(size, lastId, sortType)
        where(
            col(RepresentativeTravelJournal::user).equal(user),
        )
    }

    override fun findTargetSliceBy(
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
        targetUser: User,
        loginUserId: Long,
    ): List<RepresentativeTravelJournal> = queryFactory.listQuery {
        val followingIds = getFollowingsSubQuery(loginUserId)
        getRepTravelJournalSliceBaseQuery(size, lastId, sortType)
        associate(
            entity(RepresentativeTravelJournal::class),
            entity(TravelJournal::class),
            on(RepresentativeTravelJournal::travelJournal),
        )
        where(
            and(
                col(RepresentativeTravelJournal::user).equal(targetUser),
                or(
                    nestedCol(col(TravelJournal::travelJournalInformation), TravelJournalInformation::visibility).equal(
                        TravelJournalVisibility.PUBLIC,
                    ),
                    and(
                        nestedCol(
                            col(TravelJournal::travelJournalInformation),
                            TravelJournalInformation::visibility,
                        ).equal(
                            TravelJournalVisibility.FRIEND_ONLY,
                        ),
                        nestedCol(col(TravelJournal::user), User::id).`in`(followingIds),
                    ),
                ),
            ),
        )
    }

    private fun CriteriaQueryDsl<RepresentativeTravelJournal>.getRepTravelJournalSliceBaseQuery(
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
    ) {
        select(RepresentativeTravelJournal::class.java)
        from(RepresentativeTravelJournal::class.java)
        where(
            dynamicPredicateByLastId(lastId, sortType),
        )
        orderBy(dynamicOrderingSortType(sortType))
        limit(size + 1)
    }

    private fun CriteriaQueryDsl<RepresentativeTravelJournal>.dynamicPredicateByLastId(
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
    ) = if (lastId != null) {
        when (sortType) {
            RepresentativeTravelJournalSortType.LATEST -> col(RepresentativeTravelJournal::id).lessThan(lastId)
        }
    } else {
        PredicateSpec.empty
    }

    private fun CriteriaQueryDsl<RepresentativeTravelJournal>.dynamicOrderingSortType(sortType: RepresentativeTravelJournalSortType): List<OrderSpec> =
        when (sortType) {
            RepresentativeTravelJournalSortType.LATEST -> listOf(col(RepresentativeTravelJournal::id).desc())
        }

    private fun getFollowingsSubQuery(userId: Long) = queryFactory.subquery<Long> {
        select(nestedCol(col(Follow::following), User::id))
        from(entity(Follow::class))
        where(nestedCol(col(Follow::follower), User::id).equal(userId))
    }
}

enum class RepresentativeTravelJournalSortType(private val description: String) {
    LATEST("최신순"),
}
