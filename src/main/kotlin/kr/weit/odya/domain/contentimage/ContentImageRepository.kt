package kr.weit.odya.domain.contentimage

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun ContentImageRepository.getImageByUserId(userId: Long, size: Int, lastId: Long?) =
    findSliceByUserId(userId, size, lastId)

fun ContentImageRepository.getLifeShotByUserId(userId: Long, size: Int, lastId: Long?) =
    findLifeShotSliceByUserId(userId, size, lastId)

fun ContentImageRepository.getImageById(id: Long) = findByIdOrNull(id) ?: throw NoSuchElementException("사진이 존재하지 않습니다.")

interface ContentImageRepository : JpaRepository<ContentImage, Long>, CustomContentImageRepository {
    fun findAllByUserId(userId: Long): List<ContentImage>

    fun deleteAllByUserId(userId: Long)
}

interface CustomContentImageRepository {
    fun findSliceByUserId(userId: Long, size: Int, lastId: Long?): List<ContentImage>
    fun findLifeShotSliceByUserId(userId: Long, size: Int, lastId: Long?): List<ContentImage>
}

class CustomContentImageRepositoryImpl(private val queryFactory: QueryFactory) : CustomContentImageRepository {
    override fun findSliceByUserId(userId: Long, size: Int, lastId: Long?): List<ContentImage> =
        queryFactory.listQuery {
            getImagesSliceBaseQuery(userId, size, lastId)
        }

    override fun findLifeShotSliceByUserId(userId: Long, size: Int, lastId: Long?): List<ContentImage> =
        queryFactory.listQuery {
            getImagesSliceBaseQuery(userId, size, lastId)
            where(col(ContentImage::isLifeShot).equal(true))
        }

    private fun CriteriaQueryDsl<ContentImage>.getImagesSliceBaseQuery(userId: Long, size: Int, lastId: Long?) {
        select(entity(ContentImage::class))
        from(entity(ContentImage::class))
        associate(ContentImage::class, entity(User::class), on(ContentImage::user))
        where(
            and(
                col(User::id).equal(userId),
                if (lastId != null) {
                    col(ContentImage::id).lessThan(lastId)
                } else {
                    PredicateSpec.empty
                },
            ),
        )
        orderBy(col(ContentImage::id).desc())
        limit(size + 1)
    }
}
