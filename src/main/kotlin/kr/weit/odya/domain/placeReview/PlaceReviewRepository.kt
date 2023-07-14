package kr.weit.odya.domain.placeReview

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.query.spec.predicate.PredicateSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun PlaceReviewRepository.getByPlaceReviewId(id: Long): PlaceReview =
    findByIdOrNull(id) ?: throw NoSuchElementException("$id: 존재하지 않는 장소 리뷰입니다.")

fun PlaceReviewRepository.getPlaceReviewListByPlaceId(
    placeId: String,
    size: Int,
    sortType: PlaceReviewSortType,
    lastId: Long?,
): List<PlaceReview> =
    findSliceByPlaceIdOrderBySortType(placeId, size, sortType, lastId)

fun PlaceReviewRepository.getPlaceReviewListByUser(
    user: User,
    size: Int,
    sortType: PlaceReviewSortType,
    lastId: Long?,
): List<PlaceReview> =
    findSliceByUserOrderBySortType(user, size, sortType, lastId)

@Repository
interface PlaceReviewRepository : JpaRepository<PlaceReview, Long>, CustomPlaceReviewRepository {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean
}

interface CustomPlaceReviewRepository {
    fun findSliceByPlaceIdOrderBySortType(
        placeId: String,
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): List<PlaceReview>

    fun findSliceByUserOrderBySortType(
        user: User,
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): List<PlaceReview>
}

class PlaceReviewRepositoryImpl(private val queryFactory: QueryFactory) : CustomPlaceReviewRepository {
    override fun findSliceByPlaceIdOrderBySortType(
        placeId: String,
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): List<PlaceReview> = queryFactory.listQuery {
        baseSearchQuery(size, sortType, lastId)
        where(col(PlaceReview::placeId).equal(placeId))
    }

    override fun findSliceByUserOrderBySortType(
        user: User,
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): List<PlaceReview> = queryFactory.listQuery {
        baseSearchQuery(size, sortType, lastId)
        where(col(PlaceReview::user).equal(user))
    }

    private fun CriteriaQueryDsl<PlaceReview>.baseSearchQuery(
        size: Int,
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ) {
        select(entity(PlaceReview::class))
        from(entity(PlaceReview::class))
        where(dynamicPredicatePlaceReviewSortType(sortType, lastId))
        orderBy(dynamicOrderingByPlaceReviewSortType(sortType))
        limit(size + 1)
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicPredicatePlaceReviewSortType(
        sortType: PlaceReviewSortType,
        lastId: Long?,
    ): PredicateSpec {
        return if (lastId != null) {
            when (sortType) {
                PlaceReviewSortType.LATEST -> col(PlaceReview::id).lessThan(lastId)
                PlaceReviewSortType.OLDEST -> col(PlaceReview::id).greaterThan(lastId)
            }
        } else {
            PredicateSpec.empty
        }
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicOrderingByPlaceReviewSortType(
        sortType: PlaceReviewSortType,
    ): List<OrderSpec> =
        when (sortType) {
            PlaceReviewSortType.LATEST -> listOf(col(PlaceReview::id).desc())
            PlaceReviewSortType.OLDEST -> listOf(col(PlaceReview::id).asc())
        }
}

enum class PlaceReviewSortType(val description: String) {
    LATEST("최신순"), OLDEST("오래된순")
}
