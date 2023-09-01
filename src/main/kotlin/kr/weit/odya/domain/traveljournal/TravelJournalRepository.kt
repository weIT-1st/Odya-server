package kr.weit.odya.domain.traveljournal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun TravelJournalRepository.getByUserId(userId: Long): List<TravelJournal> = findAllByUserId(userId)

fun TravelJournalRepository.getByTravelJournalId(travelJournalId: Long): TravelJournal =
    findByIdOrNull(travelJournalId) ?: throw NoSuchElementException("$travelJournalId : 해당 여행 일지가 존재하지 않습니다.")

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long> {
    fun findAllByUserId(userId: Long): List<TravelJournal>

    fun deleteAllByUserId(userId: Long)
}
