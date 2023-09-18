package kr.weit.odya.domain.report

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.deleteQuery
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun ReportCommunityRepository.existsByCommunityIdAndUserId(communityId: Long, userId: Long) =
    existsByCommunityIdAndCommonReportInformationUserId(communityId, userId)

fun ReportCommunityRepository.deleteAllByUserId(userId: Long) {
    deleteAllByCommonReportInformationUserId(userId)
    deleteReportCommunitiesByUserId(userId)
}

@Repository
interface ReportCommunityRepository : JpaRepository<ReportCommunity, Long>, CustomReportCommunityRepository {
    fun existsByCommunityIdAndCommonReportInformationUserId(placeReviewId: Long, userId: Long): Boolean

    fun countAllByCommunityId(communityId: Long): Int

    fun deleteAllByCommunityId(communityId: Long)

    fun deleteAllByCommonReportInformationUserId(userId: Long)

    fun deleteAllByCommunityIdIn(communityIds: List<Long>)
}

interface CustomReportCommunityRepository {
    fun deleteReportCommunitiesByUserId(userId: Long): Int
}

class CustomReportCommunityRepositoryImpl(private val queryFactory: QueryFactory) : CustomReportCommunityRepository {
    override fun deleteReportCommunitiesByUserId(userId: Long): Int = queryFactory.deleteQuery<ReportCommunity> {
        where(col(ReportCommunity::community).`in`(reportCommunityByUserIdSubQuery(userId)))
    }.executeUpdate()

    private fun reportCommunityByUserIdSubQuery(userId: Long): List<Community> = queryFactory.listQuery {
        select(entity(Community::class))
        from(entity(Community::class))
        where(nestedCol(col(Community::user), User::id).equal(userId))
    }
}
