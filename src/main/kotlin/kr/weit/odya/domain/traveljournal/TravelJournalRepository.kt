package kr.weit.odya.domain.traveljournal

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.contentimage.ContentImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun TravelJournalRepository.getByUserId(userId: Long): List<TravelJournal> = findAllByUserId(userId)

fun TravelJournalRepository.getByTravelJournalId(travelJournalId: Long): TravelJournal =
    findByIdOrNull(travelJournalId) ?: throw NoSuchElementException("$travelJournalId : 해당 여행 일지가 존재하지 않습니다.")

fun TravelJournalRepository.getByContentImageNames(travelJournalId: Long): List<String> =
    findContentImageNameListById(travelJournalId)

@Repository
interface TravelJournalRepository : JpaRepository<TravelJournal, Long>, CustomTravelJournalRepository {
    fun findAllByUserId(userId: Long): List<TravelJournal>

    fun deleteAllByUserId(userId: Long)
}
interface CustomTravelJournalRepository {
    fun findContentImageNameListById(
        travelJournalId: Long,
    ): List<String>
}

class TravelJournalRepositoryImpl(private val queryFactory: QueryFactory) : CustomTravelJournalRepository {
    override fun findContentImageNameListById(
        travelJournalId: Long,
    ): List<String> = queryFactory.listQuery {
        select(col(ContentImage::name))
        from(entity(TravelJournal::class))
        associate(TravelJournal::class, TravelJournalContent::class, on(TravelJournal::travelJournalContents))
        associate(TravelJournalContent::class, TravelJournalContentImage::class, on(TravelJournalContent::travelJournalContentImages))
        associate(TravelJournalContentImage::class, ContentImage::class, on(TravelJournalContentImage::contentImage))
        where(col(TravelJournal::id).equal(travelJournalId))
    }
}
