package kr.weit.odya.domain.topic

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import org.springframework.data.jpa.repository.JpaRepository

fun TopicRepository.getTopic(size: Int, lastId: Long?): List<Topic> {
    return sliceByLastId(size, lastId)
}

interface TopicRepository : JpaRepository<Topic, Long>, CustomTopicRepository

interface CustomTopicRepository {
    fun sliceByLastId(
        size: Int,
        lastId: Long?,
    ): List<Topic>
}

class TopicRepositoryImpl(private val queryFactory: QueryFactory) : CustomTopicRepository {
    override fun sliceByLastId(
        size: Int,
        lastId: Long?,
    ): List<Topic> = queryFactory.listQuery {
        select(entity(Topic::class))
        from(entity(Topic::class))
        where(dynamicPredicateLastId(lastId))
        limit(size + 1)
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicPredicateLastId(
        lastId: Long?,
    ): PredicateSpec {
        return if (lastId != null) {
            col(Topic::id).greaterThan(lastId)
        } else {
            PredicateSpec.empty
        }
    }
}
