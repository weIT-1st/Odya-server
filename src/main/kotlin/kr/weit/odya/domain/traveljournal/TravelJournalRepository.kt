package kr.weit.odya.domain.traveljournal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun TravelJournalRepository.getByTravelJournalId(id: Long): TravelJournal =
    findByIdOrNull(id) ?: throw NoSuchElementException("$id: 존재하지 않는 여행 일지입니다.")

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long>
