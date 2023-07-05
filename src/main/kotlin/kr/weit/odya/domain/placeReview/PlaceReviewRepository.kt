package kr.weit.odya.domain.placeReview

import com.linecorp.kotlinjdsl.QueryFactory
import com.linecorp.kotlinjdsl.listQuery
import com.linecorp.kotlinjdsl.query.spec.OrderSpec
import com.linecorp.kotlinjdsl.querydsl.CriteriaQueryDsl
import com.linecorp.kotlinjdsl.querydsl.expression.col
import kr.weit.odya.domain.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun PlaceReviewRepository.getByPlaceReviewId(id: Long): PlaceReview =
    findByIdOrNull(id) ?: throw NoSuchElementException("$id: 존재하지 않는 장소 리뷰입니다.")

fun PlaceReviewRepository.getPlaceReviewListByPlaceId(
    placeId: String,
    pageable: Pageable,
    sortType: PlaceReviewSortType
): List<PlaceReview> =
    findSliceByPlaceIdOrderBySortType(placeId, pageable, sortType)

fun PlaceReviewRepository.getPlaceReviewListByUser(
    user: User,
    pageable: Pageable,
    sortType: PlaceReviewSortType
): List<PlaceReview> =
    findSliceByUserOrderBySortType(user, pageable, sortType)

@Repository
interface PlaceReviewRepository : JpaRepository<PlaceReview, Long>, CustomPlaceReviewRepository {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean
}

interface CustomPlaceReviewRepository {
    fun findSliceByPlaceIdOrderBySortType(
        placeId: String,
        pageable: Pageable,
        sortType: PlaceReviewSortType
    ): List<PlaceReview>

    fun findSliceByUserOrderBySortType(
        user: User,
        pageable: Pageable,
        sortType: PlaceReviewSortType
    ): List<PlaceReview>
}

class PlaceReviewRepositoryImpl(private val queryFactory: QueryFactory) : CustomPlaceReviewRepository {
    override fun findSliceByPlaceIdOrderBySortType(
        placeId: String,
        pageable: Pageable,
        sortType: PlaceReviewSortType
    ): List<PlaceReview> = queryFactory.listQuery {
        select(entity(PlaceReview::class))
        from(entity(PlaceReview::class))
        where(col(entity(PlaceReview::class), PlaceReview::placeId).equal(placeId))
        orderBy(dynamicOrderingByPlaceReviewSortType(sortType))
        offset(pageable.offset.toInt())
        limit(pageable.pageSize + 1)
    }

    override fun findSliceByUserOrderBySortType(
        user: User,
        pageable: Pageable,
        sortType: PlaceReviewSortType
    ): List<PlaceReview> = queryFactory.listQuery {
        select(entity(PlaceReview::class))
        from(entity(PlaceReview::class))
        where(col(PlaceReview::user).equal(user))
        orderBy(dynamicOrderingByPlaceReviewSortType(sortType))
        offset(pageable.offset.toInt())
        limit(pageable.pageSize + 1)
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicOrderingByPlaceReviewSortType(
        sortType: PlaceReviewSortType
    ): List<OrderSpec> =
        when (sortType) {
            PlaceReviewSortType.LATEST -> listOf(col(PlaceReview::id).desc())
            PlaceReviewSortType.OLDEST -> listOf(col(PlaceReview::id).asc())
        }
}

enum class PlaceReviewSortType(val description: String) {
    LATEST("최신순"), OLDEST("오래된순")
}
