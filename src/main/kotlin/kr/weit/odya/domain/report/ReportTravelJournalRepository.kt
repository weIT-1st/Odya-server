package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
fun ReportTravelJournalRepository.countReportTravelJournal(travelJournalId: Long): Boolean {
    if (countByTravelJournalId(travelJournalId) == 5) {
        deleteByTravelJournalId(travelJournalId)
        return true
    }
    return false
}

@Repository
interface ReportTravelJournalRepository : JpaRepository<ReportTravelJournal, Long> {
    fun countByTravelJournalId(travelJournalId: Long): Int

    fun deleteByTravelJournalId(travelJournalId: Long)
}
