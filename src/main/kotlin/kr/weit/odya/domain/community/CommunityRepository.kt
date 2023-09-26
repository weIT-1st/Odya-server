package kr.weit.odya.domain.community

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
import com.linecorp.kotlinjdsl.updateQuery
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun CommunityRepository.getByCommunityId(communityId: Long): Community =
    findByIdOrNull(communityId) ?: throw NoSuchElementException("$communityId: 존재하지 않는 커뮤니티입니다.")

fun CommunityRepository.getCommunitySliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    sortType: CommunitySortType,
): List<Community> = findCommunitySliceBy(userId, size, lastId, sortType)

fun CommunityRepository.getMyCommunitySliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    sortType: CommunitySortType,
): List<Community> = findMyCommunitySliceBy(userId, size, lastId, sortType)

fun CommunityRepository.getFriendCommunitySliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
    sortType: CommunitySortType,
): List<Community> = findFriendCommunitySliceBy(userId, size, lastId, sortType)

fun CommunityRepository.getImageNamesById(communityId: Long): List<String> =
    findContentImageNameListById(communityId)

@Repository
interface CommunityRepository : JpaRepository<Community, Long>, CustomCommunityRepository {
    fun deleteAllByUserId(userId: Long)
}

interface CustomCommunityRepository {
    fun findCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): List<Community>

    fun findMyCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): List<Community>

    fun findFriendCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): List<Community>

    fun findContentImageNameListById(communityId: Long): List<String>

    fun updateTravelJournalIdToNull(travelJournalId: Long)
}

class CommunityRepositoryImpl(private val queryFactory: QueryFactory) : CustomCommunityRepository {
    override fun findCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): List<Community> = queryFactory.listQuery {
        getCommunitySliceBaseQuery(lastId, sortType, size)
        val followingIds = getFollowingIdsSubQuery(userId)
        val publicCommunityIds = getPublicCommunityIdsSubQuery()
        val friendOnlyCommunityIds = getFriendOnlyCommunityIdsSubQuery(followingIds)
        val myFriendOnlyCommunityIds = getMyFriendOnlyCommunityIdsSubQuery(userId)
        where(
            or(
                col(Community::id).`in`(publicCommunityIds),
                col(Community::id).`in`(friendOnlyCommunityIds),
                col(Community::id).`in`(myFriendOnlyCommunityIds),
            ),
        )
    }

    override fun findMyCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): List<Community> = queryFactory.listQuery {
        getCommunitySliceBaseQuery(lastId, sortType, size)
        where(nestedCol(col(Community::user), User::id).equal(userId))
    }

    override fun findFriendCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): List<Community> = queryFactory.listQuery {
        val followingIds = getFollowingIdsSubQuery(userId)
        getCommunitySliceBaseQuery(lastId, sortType, size)
        where(nestedCol(col(Community::user), User::id).`in`(followingIds))
    }

    override fun findContentImageNameListById(communityId: Long): List<String> = queryFactory.listQuery {
        select(col(ContentImage::name))
        from(entity(Community::class))
        associate(
            entity(Community::class),
            entity(CommunityContentImage::class),
            Relation<Community, CommunityContentImage>("mutableCommunityContentImages"),
        )
        associate(
            entity(CommunityContentImage::class),
            entity(ContentImage::class),
            on(CommunityContentImage::contentImage),
        )
        where(col(Community::id).equal(communityId))
    }

    override fun updateTravelJournalIdToNull(travelJournalId: Long) {
        queryFactory.updateQuery<Community> {
            associate(Community::class, TravelJournal::class, on(Community::travelJournal))
            where(
                col(TravelJournal::id).equal(travelJournalId),
            )
            set(col(Community::travelJournal), null)
        }.executeUpdate()
    }

    private fun getMyFriendOnlyCommunityIdsSubQuery(userId: Long) = queryFactory.subquery<Long> {
        val c3Entity = entity(Community::class, "c3")
        val ci3Entity = entity(CommunityInformation::class, "ci3")
        associate(
            c3Entity,
            ci3Entity,
            on(Community::communityInformation),
        )
        select(col(c3Entity, Community::id))
        from(c3Entity)
        where(
            and(
                col(ci3Entity, CommunityInformation::visibility).equal(CommunityVisibility.FRIEND_ONLY),
                nestedCol(col(c3Entity, Community::user), User::id).equal(userId),
            ),
        )
    }

    private fun getFriendOnlyCommunityIdsSubQuery(followingIds: SubqueryExpressionSpec<Long>) =
        queryFactory.subquery<Long> {
            val c2Entity = entity(Community::class, "c2")
            val ci2Entity = entity(CommunityInformation::class, "ci2")
            associate(
                c2Entity,
                ci2Entity,
                on(Community::communityInformation),
            )
            select(col(c2Entity, Community::id))
            from(c2Entity)
            where(
                and(
                    col(ci2Entity, CommunityInformation::visibility).equal(CommunityVisibility.FRIEND_ONLY),
                    nestedCol(col(c2Entity, Community::user), User::id).`in`(followingIds),
                ),
            )
        }

    private fun getPublicCommunityIdsSubQuery() = queryFactory.subquery<Long> {
        val c1Entity = entity(Community::class, "c1")
        val ci1Entity = entity(CommunityInformation::class, "ci1")
        associate(
            c1Entity,
            ci1Entity,
            on(Community::communityInformation),
        )
        select(col(c1Entity, Community::id))
        from(c1Entity)
        where(col(ci1Entity, CommunityInformation::visibility).equal(CommunityVisibility.PUBLIC))
    }

    private fun getFollowingIdsSubQuery(userId: Long) = queryFactory.subquery<Long> {
        select(nestedCol(col(Follow::following), User::id))
        from(entity(Follow::class))
        where(nestedCol(col(Follow::follower), User::id).equal(userId))
    }

    private fun CriteriaQueryDsl<Community>.getCommunitySliceBaseQuery(
        lastId: Long?,
        sortType: CommunitySortType,
        size: Int,
    ) {
        select(entity(Community::class))
        from(entity(Community::class))
        associate(
            entity(Community::class),
            entity(CommunityInformation::class),
            on(Community::communityInformation),
        )
        where(dynamicPredicateByLastId(lastId, sortType))
        orderBy(dynamicOrderingSortType(sortType))
        limit(size + 1)
    }

    private fun CriteriaQueryDsl<Community>.dynamicPredicateByLastId(
        lastId: Long?,
        sortType: CommunitySortType,
    ) = if (lastId != null) {
        when (sortType) {
            CommunitySortType.LATEST -> col(Community::id).lessThan(lastId)
            CommunitySortType.LIKE -> {
                val likeCount = getCommunityLikeCountByLastId(lastId)
                or(
                    and(col(Community::likeCount).equal(likeCount), col(Community::id).lessThan(lastId)),
                    col(Community::likeCount).lessThan(likeCount),
                )
            }
        }
    } else {
        PredicateSpec.empty
    }

    private fun getCommunityLikeCountByLastId(lastId: Long): Int = queryFactory.selectQuery<Int> {
        select(col(Community::likeCount))
        from(entity(Community::class))
        where(col(Community::id).equal(lastId))
    }.singleResult

    private fun CriteriaQueryDsl<Community>.dynamicOrderingSortType(
        sortType: CommunitySortType,
    ): List<OrderSpec> =
        when (sortType) {
            CommunitySortType.LATEST -> listOf(col(Community::id).desc())
            CommunitySortType.LIKE -> listOf(col(Community::likeCount).desc(), col(Community::id).desc())
        }

    companion object {
        fun QueryFactory.communityByUserIdSubQuery(userId: Long) =
            subquery {
                select(col(Community::id))
                from(entity(Community::class))
                where(nestedCol(col(Community::user), User::id).equal(userId))
            }
    }
}

enum class CommunitySortType(val description: String) {
    LATEST("최신순"), LIKE("좋아요순")
}
