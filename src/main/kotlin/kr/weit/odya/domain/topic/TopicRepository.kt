package kr.weit.odya.domain.topic

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.community.Community
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun TopicRepository.getByTopicId(topicId: Long): Topic =
    findByIdOrNull(topicId) ?: throw NoSuchElementException("$topicId : 해당 토픽이 존재하지 않습니다.")

fun TopicRepository.getPopularTopicsAtPlace(placeId: String, size: Int): List<Topic> =
    findPopularTopicsByPlaceIdAndSize(placeId, size)

interface TopicRepository : JpaRepository<Topic, Long>, TopicCustomRepository

interface TopicCustomRepository {

    fun findPopularTopicsByPlaceIdAndSize(placeId: String, size: Int): List<Topic>
}

class TopicCustomRepositoryImpl(private val queryFactory: QueryFactory) : TopicCustomRepository {
    override fun findPopularTopicsByPlaceIdAndSize(placeId: String, size: Int): List<Topic> = queryFactory.listQuery {
        select(entity(Topic::class))
        from(entity(Community::class))
        associate(Community::class, entity(Topic::class), on(Community::topic))
        where(col(Community::placeId).equal(placeId))
        groupBy(entity(Topic::class))
        orderBy(count(entity(Topic::class)).desc())
        limit(size)
    }
}
