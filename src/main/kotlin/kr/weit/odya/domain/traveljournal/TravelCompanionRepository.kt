package kr.weit.odya.domain.traveljournal

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

fun TravelCompanionRepository.cancelTravelCompanion(userId: Long, travelJournalId: Long) =
    deleteAllByUserIdAndTravelJournalId(userId, travelJournalId)

fun TravelCompanionRepository.existsTravelCompanion(userId: Long, travelJournalId: Long) =
    existsAllByUserIdAndTravelJournalId(userId, travelJournalId)

@Repository
interface TravelCompanionRepository : JpaRepository<TravelCompanion, Long> {
    @Modifying
    @Query("delete from TravelCompanion where user.id = :userId")
    fun deleteAllByUserId(userId: Long)

    @Query("select(exists()) from TravelCompanion join TravelJournal.id where TravelJournal.id = :travelJournalId and TravelCompanion.user.id= :userId")
    fun deleteAllByUserIdAndTravelJournalId(userId: Long, travelJournalId: Long)

    @Query("select(exists()) from TravelCompanion join TravelJournal.id where TravelJournal.id = :travelJournalId and TravelCompanion.user.id= :userId")
    fun existsAllByUserIdAndTravelJournalId(userId: Long, travelJournalId: Long): Boolean
}

/*
interface CustomTravelCompanionRepository {
    fun existsAllByUserIdAndTravelJournalId(userId: Long, travelJournalId: Long): Boolean
}
class CustomTravelCompanionRepositoryImpl(private val queryFactory: QueryFactory) : CustomTravelCompanionRepository {
    override fun existsAllByUserIdAndTravelJournalId(userId: Long, travelJournalId: Long): Boolean {
        queryFactory.streamQuery<Boolean> {
            exists(entity(TravelCompanion::class))
            from(entity(TravelCompanion::class))
            associate(entity(TravelJournal::class), entity(TravelCompanion::class), on(TravelJournal::travelCompanions))
            where(
                or(
                    queryFactory.singleQuery { select(TravelCompanion::user)
                    from(TravelCompanion::class)
                    where(
                        or(
                            nestedCol(col(TravelCompanion::user)),User::id).equal(userId))
                    }
                )
                and(
                    User::id.equals(userId)},
                    col(TravelJournal::id).equals(travelJournalId),
                ),
            )
        }
    }
}
*/
