package kr.weit.odya.domain.report

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
fun ReportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId: Long, userId: Long) =
    existsByTravelJournalIdAndCommonReportInformationUserId(travelJournalId, userId)

fun ReportTravelJournalRepository.deleteAllByUserId(userId: Long) {
    deleteAllByCommonReportInformationUserId(userId)
    deleteTravelJournalByUserId(userId)
}

@Repository
interface ReportTravelJournalRepository : JpaRepository<ReportTravelJournal, Long> {
    fun countAllByTravelJournalId(travelJournalId: Long): Int

    fun deleteAllByTravelJournalId(travelJournalId: Long)

    fun existsByTravelJournalIdAndCommonReportInformationUserId(travelJournalId: Long, userId: Long): Boolean

    fun deleteAllByCommonReportInformationUserId(userId: Long)

    @Modifying
    @Query("delete from ReportTravelJournal rtj where rtj.travelJournal.id in (select t.id from TravelJournal t where t.user.id = :userId)")
    fun deleteTravelJournalByUserId(userId: Long)
}
