package kr.weit.odya.domain.report

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.deleteQuery
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun ReportPlaceReviewRepository.existsByReviewAndUserId(placeReviewId: Long, userId: Long) =
    existsByPlaceReviewIdAndCommonReportInformationUserId(placeReviewId, userId)

fun ReportPlaceReviewRepository.deleteAllByUserId(userId: Long) {
    deleteAllByCommonReportInformationUserId(userId)
    deleteReportPlaceReviewByUserId(userId)
}

@Repository
interface ReportPlaceReviewRepository : JpaRepository<ReportPlaceReview, Long>, CustomReportPlaceReviewRepository {
    fun countAllByPlaceReviewId(placeReviewId: Long): Int

    fun deleteAllByPlaceReviewId(placeReviewId: Long)

    fun existsByPlaceReviewIdAndCommonReportInformationUserId(placeReviewId: Long, userId: Long): Boolean

    fun deleteAllByCommonReportInformationUserId(userId: Long)
}

interface CustomReportPlaceReviewRepository {
    fun deleteReportPlaceReviewByUserId(userId: Long)
}

class CustomReportPlaceReviewRepositoryImpl(private val queryFactory: QueryFactory) : CustomReportPlaceReviewRepository {
    override fun deleteReportPlaceReviewByUserId(userId: Long) {
        queryFactory.deleteQuery<ReportPlaceReview> {
            where(col(ReportPlaceReview::placeReview).`in`(placeReviewByUserIdSubQuery(userId)))
        }.executeUpdate()
    }

    private fun placeReviewByUserIdSubQuery(userId: Long): List<PlaceReview> = queryFactory.listQuery {
        select(entity(PlaceReview::class))
        from(entity(PlaceReview::class))
        where(nestedCol(col(PlaceReview::user), User::id).equal(userId))
    }
}
