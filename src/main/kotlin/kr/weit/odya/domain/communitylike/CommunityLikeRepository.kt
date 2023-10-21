package kr.weit.odya.domain.communitylike

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.deleteQuery
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import com.linecorp.kotlinjdsl.selectQuery
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepositoryImpl.Companion.communityByUserIdSubQuery
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun CommunityLikeRepository.deleteCommunityLikes(userId: Long) {
    deleteAllByUserId(userId)
    deleteCommunityLikesByUserId(userId)
}

fun CommunityLikeRepository.getLikedCommunitySliceBy(
    userId: Long,
    size: Int,
    lastId: Long?,
): List<CommunityLike> = findLikedCommunitySliceBy(userId, size, lastId)

@Repository
interface CommunityLikeRepository : JpaRepository<CommunityLike, CommunityLikeId>, CustomCommunityLikeRepository {
    fun countByUserId(userId: Long): Int

    fun deleteAllByCommunityId(communityId: Long)

    fun deleteAllByUserId(userId: Long)

    fun existsByCommunityIdAndUserId(communityId: Long, userId: Long): Boolean
}

interface CustomCommunityLikeRepository {
    fun deleteCommunityLikesByUserId(userId: Long)

    fun findLikedCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
    ): List<CommunityLike>
}

class CommunityLikeRepositoryImpl(private val queryFactory: QueryFactory) : CustomCommunityLikeRepository {
    override fun deleteCommunityLikesByUserId(userId: Long) {
        queryFactory.deleteQuery<CommunityLike> {
            val subQuery = queryFactory.communityByUserIdSubQuery(userId)
            where(nestedCol(col(CommunityLike::community), Community::id).`in`(subQuery))
        }.executeUpdate()
    }

    override fun findLikedCommunitySliceBy(
        userId: Long,
        size: Int,
        lastId: Long?,
    ): List<CommunityLike> = queryFactory.listQuery {
        select(entity(CommunityLike::class))
        from(entity(CommunityLike::class))
        associate(CommunityLike::class, entity(Community::class), on(CommunityLike::community))
        where(
            and(
                nestedCol(col(CommunityLike::user), User::id).equal(userId),
                dynamicPredicateByLastId(lastId),
            ),
        )
        orderBy(col(CommunityLike::createdDate).desc())
    }

    private fun CriteriaQueryDsl<CommunityLike>.dynamicPredicateByLastId(
        lastId: Long?,
    ) = if (lastId != null) {
        val createdDate = queryFactory.selectQuery {
            select(col(CommunityLike::createdDate))
            from(entity(CommunityLike::class))
            where(nestedCol(col(CommunityLike::community), Community::id).equal(lastId))
        }.singleResult
        col(CommunityLike::createdDate).lessThan(createdDate)
    } else {
        PredicateSpec.empty
    }
}
