package kr.weit.odya.domain.communitylike

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.deleteQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepositoryImpl.Companion.communityByUserIdSubQuery
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun CommunityLikeRepository.deleteCommunityLikes(userId: Long) {
    deleteAllByUserId(userId)
    deleteCommunityLikesByUserId(userId)
}

@Repository
interface CommunityLikeRepository : JpaRepository<CommunityLike, CommunityLikeId>, CustomCommunityLikeRepository {
    fun countByUserId(userId: Long): Int

    fun deleteAllByCommunityId(communityId: Long)

    fun deleteAllByUserId(userId: Long)

    fun existsByCommunityIdAndUserId(communityId: Long, userId: Long): Boolean
}

interface CustomCommunityLikeRepository {
    fun deleteCommunityLikesByUserId(userId: Long)
}

class CommunityLikeRepositoryImpl(private val queryFactory: QueryFactory) : CustomCommunityLikeRepository {
    override fun deleteCommunityLikesByUserId(userId: Long) {
        queryFactory.deleteQuery<CommunityLike> {
            val subQuery = queryFactory.communityByUserIdSubQuery(userId)
            where(nestedCol(col(CommunityLike::community), Community::id).`in`(subQuery))
        }.executeUpdate()
    }
}
