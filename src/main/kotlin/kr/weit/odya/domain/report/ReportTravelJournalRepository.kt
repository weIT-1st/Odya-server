package kr.weit.odya.domain.report

import kr.weit.odya.domain.traveljournal.TravelJournal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportTravelJournalRepository : JpaRepository<ReportTravelJournal, Long> {
    fun countAllByTravelJournalId(travelJournalId: Long): Int

    fun deleteAllByTravelJournalId(travelJournalId: Long)

    fun existsByTravelJournalIdAndUserId(travelJournalId: Long, userId: Long): Boolean

    fun deleteAllByUserId(userId: Long)

    fun deleteAllByTravelJournalIn(travelJournals: List<TravelJournal>)
}
