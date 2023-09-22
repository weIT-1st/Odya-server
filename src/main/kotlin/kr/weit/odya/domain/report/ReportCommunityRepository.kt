package kr.weit.odya.domain.report

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.deleteQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepositoryImpl.Companion.communityByUserIdSubQuery
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
}

interface CustomReportCommunityRepository {
    fun deleteReportCommunitiesByUserId(userId: Long)
}

class CustomReportCommunityRepositoryImpl(private val queryFactory: QueryFactory) : CustomReportCommunityRepository {
    override fun deleteReportCommunitiesByUserId(userId: Long) { queryFactory.deleteQuery<ReportCommunity> {
        val subQuery = queryFactory.communityByUserIdSubQuery(userId)
        where(nestedCol(col(ReportCommunity::community), Community::id).`in`(subQuery))
    }.executeUpdate()
    }
}
