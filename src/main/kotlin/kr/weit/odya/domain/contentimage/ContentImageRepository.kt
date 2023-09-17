package kr.weit.odya.domain.contentimage

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.CoordinateImageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun ContentImageRepository.getImageByUserId(userId: Long, size: Int, lastId: Long?) =
    findSliceByUserId(userId, size, lastId)

fun ContentImageRepository.getLifeShotByUserId(userId: Long, size: Int, lastId: Long?) =
    findLifeShotSliceByUserId(userId, size, lastId)

fun ContentImageRepository.getImageById(id: Long) = findByIdOrNull(id) ?: throw NoSuchElementException("사진이 존재하지 않습니다.")

fun ContentImageRepository.getImageByRectangle(
    coordinateImageRequest: CoordinateImageRequest,
) = findImagesByRectangleAndSize(coordinateImageRequest)

interface ContentImageRepository : JpaRepository<ContentImage, Long>, CustomContentImageRepository {
    fun findAllByUserId(userId: Long): List<ContentImage>

    fun deleteAllByUserId(userId: Long)
}

interface CustomContentImageRepository {
    fun findSliceByUserId(userId: Long, size: Int, lastId: Long?): List<ContentImage>
    fun findLifeShotSliceByUserId(userId: Long, size: Int, lastId: Long?): List<ContentImage>

    fun findImagesByRectangleAndSize(
        coordinateImageRequest: CoordinateImageRequest,
    ): List<ContentImage>
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

    override fun findImagesByRectangleAndSize(
        coordinateImageRequest: CoordinateImageRequest,
    ): List<ContentImage> =
        queryFactory.listQuery {
            select(entity(ContentImage::class))
            from(entity(ContentImage::class))
            where(
                and(
                    col(ContentImage::coordinate).isNotNull(),
                    function(
                        "within",
                        Boolean::class.java,
                        function(
                            "rectangle",
                            Any::class.java, // 원래는 Geometry::class.java로 해야 하지만, jdsl인지 hibernate인지 둘중 하나가 에러를 발생시키므로 Any::class.java로 한다
                            literal(coordinateImageRequest.leftLongitude),
                            literal(coordinateImageRequest.bottomLatitude),
                            literal(coordinateImageRequest.rightLongitude),
                            literal(coordinateImageRequest.topLatitude),
                            literal(ContentImage.SRID_WGS84),
                        ),
                        col(ContentImage::coordinate),
                    ).equal(true),
                ),
            )
            orderBy(col(ContentImage::id).desc())
            limit(coordinateImageRequest.size)
        }

    private fun CriteriaQueryDsl<ContentImage>.getImagesSliceBaseQuery(userId: Long, size: Int, lastId: Long?) {
        select(entity(ContentImage::class))
        from(entity(ContentImage::class))
        where(
            and(
                nestedCol(col(ContentImage::user), User::id).equal(userId),
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
