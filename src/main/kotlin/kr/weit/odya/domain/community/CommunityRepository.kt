package kr.weit.odya.domain.community

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.traveljournal.TravelJournal
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun CommunityRepository.getByCommunityId(communityId: Long): Community =
    findByIdOrNull(communityId) ?: throw IllegalArgumentException("$communityId: 존재하지 않는 커뮤니티입니다.")

fun CommunityRepository.getImageNamesById(communityId: Long): List<String> =
    findContentImageNameListById(communityId)

fun CommunityRepository.getImageNamesByJournalId(travelJournalId: Long): List<String> =
    findCommunityByTravelJournalId(travelJournalId)

@Repository
interface CommunityRepository : JpaRepository<Community, Long>, CustomCommunityRepository {
    fun deleteAllByUserId(userId: Long)

    @Query("select c.id from Community c where c.travelJournal.id = :travelJournalId")
    fun findIdsByTravelJournalId(travelJournalId: Long): List<Long>

    fun deleteAllByIdIn(ids: List<Long>)
}
interface CustomCommunityRepository {
    fun findContentImageNameListById(communityId: Long): List<String>

    fun findCommunityByTravelJournalId(travelJournalId: Long): List<String>
}

class CommunityRepositoryImpl(private val queryFactory: QueryFactory) : CustomCommunityRepository {
    override fun findContentImageNameListById(communityId: Long): List<String> = queryFactory.listQuery {
        select(col(ContentImage::name))
        from(entity(Community::class))
        associate(Community::class, CommunityContentImage::class, on(Community::communityContentImages))
        associate(CommunityContentImage::class, ContentImage::class, on(CommunityContentImage::contentImage))
        where(col(Community::id).equal(communityId))
    }

    override fun findCommunityByTravelJournalId(travelJournalId: Long): List<String> = queryFactory.listQuery {
        select(col(ContentImage::name))
        from(entity(Community::class))
        associate(Community::class, CommunityContentImage::class, on(Community::communityContentImages))
        associate(CommunityContentImage::class, ContentImage::class, on(CommunityContentImage::contentImage))
        associate(Community::class, TravelJournal::class, on(Community::travelJournal))
        where(col(TravelJournal::id).equal(travelJournalId))
    }
}
