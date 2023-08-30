package kr.weit.odya.domain.traveljournal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun TravelJournalRepository.getByUserId(userId: Long): List<TravelJournal> = findAllByUserId(userId)

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long> {
    fun findAllByUserId(userId: Long): List<TravelJournal>
}
