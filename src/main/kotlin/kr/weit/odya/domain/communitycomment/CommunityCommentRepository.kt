package kr.weit.odya.domain.communitycomment

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.community.Community
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun CommunityCommentRepository.getCommunityCommentBy(communityCommentId: Long, communityId: Long): CommunityComment =
    findByIdAndCommunityId(communityCommentId, communityId)
        ?: throw IllegalArgumentException("존재하지 않는 커뮤니티 댓글입니다.")

fun CommunityCommentRepository.getSliceCommunityCommentBy(
    communityId: Long,
    size: Int,
    lastId: Long?,
): List<CommunityComment> =
    findSliceByCommunityIdAndSizeAndLastId(communityId, size, lastId)

@Repository
interface CommunityCommentRepository : JpaRepository<CommunityComment, Long>, CustomCommunityCommentRepository {
    @EntityGraph(attributePaths = ["user"])
    fun findByIdAndCommunityId(communityCommentId: Long, communityId: Long): CommunityComment?

    fun deleteAllByCommunityId(communityId: Long)

    fun countByCommunityId(communityId: Long): Int
}

interface CustomCommunityCommentRepository {
    fun findSliceByCommunityIdAndSizeAndLastId(
        communityId: Long,
        size: Int,
        lastId: Long?,
    ): List<CommunityComment>
}

class CommunityCommentRepositoryImpl(private val queryFactory: QueryFactory) : CustomCommunityCommentRepository {
    override fun findSliceByCommunityIdAndSizeAndLastId(
        communityId: Long,
        size: Int,
        lastId: Long?,
    ): List<CommunityComment> = queryFactory.listQuery {
        select(entity(CommunityComment::class))
        from(entity(CommunityComment::class))
        associate(entity(CommunityComment::class), entity(Community::class), on(CommunityComment::community))
        where(
            and(
                col(Community::id).equal(communityId),
                dynamicPredicateByLastId(lastId),
            ),
        )
        orderBy(col(CommunityComment::id).asc())
        limit(size + 1)
    }

    private fun CriteriaQueryDsl<CommunityComment>.dynamicPredicateByLastId(
        lastId: Long?,
    ) = if (lastId != null) {
        col(CommunityComment::id).greaterThan(lastId)
    } else {
        PredicateSpec.empty
    }
}
