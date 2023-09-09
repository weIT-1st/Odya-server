package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
fun ReportTravelJournalRepository.existsByJournalAndUserId(travelJournalId: Long, userId: Long) =
    existsByTravelJournalIdAndCommonReportInformationUserId(travelJournalId, userId)

@Repository
interface ReportTravelJournalRepository : JpaRepository<ReportTravelJournal, Long> {
    fun countAllByTravelJournalId(travelJournalId: Long): Int

    fun deleteAllByTravelJournalId(travelJournalId: Long)

    fun existsByTravelJournalIdAndCommonReportInformationUserId(travelJournalId: Long, userId: Long): Boolean

    fun deleteAllByCommonReportInformationUserId(userId: Long)
}
