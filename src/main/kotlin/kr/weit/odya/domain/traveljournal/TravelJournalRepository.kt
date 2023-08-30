package kr.weit.odya.domain.traveljournal

import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

fun TravelJournalRepository.getByUser(user: User): List<TravelJournal> = findAllByUser(user)

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long> {
    fun findAllByUser(user: User): List<TravelJournal>
}
