package kr.weit.odya.domain.report

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.deleteQuery
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
fun ReportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId: Long, userId: Long) =
    existsByTravelJournalIdAndCommonReportInformationUserId(travelJournalId, userId)

fun ReportTravelJournalRepository.deleteAllByUserId(userId: Long) {
    deleteAllByCommonReportInformationUserId(userId)
    deleteReportTravelJournalByUserId(userId)
}

@Repository
interface ReportTravelJournalRepository : JpaRepository<ReportTravelJournal, Long>, CustomReportTravelJournalRepository {
    fun countAllByTravelJournalId(travelJournalId: Long): Int

    fun deleteAllByTravelJournalId(travelJournalId: Long)

    fun existsByTravelJournalIdAndCommonReportInformationUserId(travelJournalId: Long, userId: Long): Boolean

    fun deleteAllByCommonReportInformationUserId(userId: Long)
}

interface CustomReportTravelJournalRepository {
    fun deleteReportTravelJournalByUserId(userId: Long): Int
}

class CustomReportTravelJournalRepositoryImpl(private val queryFactory: QueryFactory) : CustomReportTravelJournalRepository {
    override fun deleteReportTravelJournalByUserId(userId: Long) = queryFactory.deleteQuery<ReportTravelJournal> {
        where(col(ReportTravelJournal::travelJournal).`in`(travelJournalByUserIdSubQuery(userId)))
    }.executeUpdate()

    private fun travelJournalByUserIdSubQuery(userId: Long): List<TravelJournal> = queryFactory.listQuery {
        select(entity(TravelJournal::class))
        from(entity(TravelJournal::class))
        where(nestedCol(col(TravelJournal::user), User::id).equal(userId))
    }
}
