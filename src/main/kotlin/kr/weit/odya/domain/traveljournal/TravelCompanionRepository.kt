package kr.weit.odya.domain.traveljournal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TravelCompanionRepository : JpaRepository<TravelCompanion, Long> {
    @Modifying
    @Query("delete from TravelCompanion where user.id = :userId")
    fun deleteAllByUserId(userId: Long)
}
