package kr.weit.odya.domain.community

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import com.linecorp.kotlinjdsl.subquery
import com.linecorp.kotlinjdsl.updateQuery
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun CommunityRepository.getByCommunityId(communityId: Long): Community =
    findByIdOrNull(communityId) ?: throw IllegalArgumentException("$communityId: 존재하지 않는 커뮤니티입니다.")

fun CommunityRepository.getImageNamesById(communityId: Long): List<String> =
    findContentImageNameListById(communityId)

@Repository
interface CommunityRepository : JpaRepository<Community, Long>, CustomCommunityRepository {
    fun deleteAllByUserId(userId: Long)
}

interface CustomCommunityRepository {
    fun findContentImageNameListById(communityId: Long): List<String>

    fun updateTravelJournalIdToNull(travelJournalId: Long)
}

class CommunityRepositoryImpl(private val queryFactory: QueryFactory) : CustomCommunityRepository {
    override fun findContentImageNameListById(communityId: Long): List<String> = queryFactory.listQuery {
        select(col(ContentImage::name))
        from(entity(Community::class))
        associate(entity(Community::class), entity(CommunityContentImage::class), on(Community::communityContentImages))
        associate(entity(CommunityContentImage::class), entity(ContentImage::class), on(CommunityContentImage::contentImage))
        where(col(Community::id).equal(communityId))
    }

    override fun updateTravelJournalIdToNull(travelJournalId: Long) {
        queryFactory.updateQuery<Community> {
            associate(Community::class, TravelJournal::class, on(Community::travelJournal))
            where(
                col(TravelJournal::id).equal(travelJournalId),
            )
            set(col(Community::travelJournal), null)
        }.executeUpdate()
    }

    companion object {
        fun QueryFactory.communityByUserIdSubQuery(userId: Long) =
            subquery {
                select(col(Community::id))
                from(entity(Community::class))
                where(nestedCol(col(Community::user), User::id).equal(userId))
            }
    }
}
