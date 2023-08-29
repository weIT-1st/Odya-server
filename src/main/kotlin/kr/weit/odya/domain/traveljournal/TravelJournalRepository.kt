package kr.weit.odya.domain.traveljournal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long>