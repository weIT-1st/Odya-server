package kr.weit.odya.domain.favoritePlace

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

fun FavoritePlaceRepository.getByFavoritePlaceId(favoritePlaceId: Long): FavoritePlace =
    findByIdOrNull(favoritePlaceId) ?: throw NoSuchElementException("해당 장소는 관심 장소 등록되어있지 않습니다.")

fun FavoritePlaceRepository.getByFavoritePlaceList(
    user: User,
    size: Int,
    sortType: FavoritePlaceSortType,
    lastId: Long?,
): List<FavoritePlace> =
    findSliceByUserOrderBySortType(user, size, sortType, lastId)

@Repository
interface FavoritePlaceRepository : JpaRepository<FavoritePlace, Long>, CustomFavoritePlaceRepository {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean

    fun countByUserId(userId: Long): Int
}

interface CustomFavoritePlaceRepository {
    fun findSliceByUserOrderBySortType(
        user: User,
        size: Int,
        sortType: FavoritePlaceSortType,
        lastId: Long?,
    ): List<FavoritePlace>
}

class FavoritePlaceRepositoryImpl(private val queryFactory: QueryFactory) : CustomFavoritePlaceRepository {
    override fun findSliceByUserOrderBySortType(
        user: User,
        size: Int,
        sortType: FavoritePlaceSortType,
        lastId: Long?,
    ): List<FavoritePlace> = queryFactory.listQuery {
        baseSearchQuery(size, sortType, lastId)
        where(col(FavoritePlace::user).equal(user))
    }

    private fun CriteriaQueryDsl<FavoritePlace>.baseSearchQuery(
        size: Int,
        sortType: FavoritePlaceSortType,
        lastId: Long?,
    ) {
        select(entity(FavoritePlace::class))
        from(entity(FavoritePlace::class))
        where(dynamicPredicateFavoritePlaceSortType(sortType, lastId))
        orderBy(dynamicOrderingByFavoritePlaceSortType(sortType))
        limit(size + 1)
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicPredicateFavoritePlaceSortType(
        sortType: FavoritePlaceSortType,
        lastId: Long?,
    ): PredicateSpec {
        return if (lastId != null) {
            when (sortType) {
                FavoritePlaceSortType.LATEST -> col(FavoritePlace::id).lessThan(lastId)
            }
        } else {
            PredicateSpec.empty
        }
    }

    private fun <T> CriteriaQueryDsl<T>.dynamicOrderingByFavoritePlaceSortType(
        sortType: FavoritePlaceSortType,
    ): List<OrderSpec> =
        when (sortType) {
            FavoritePlaceSortType.LATEST -> listOf(col(FavoritePlace::id).desc())
        }
}

enum class FavoritePlaceSortType(val description: String) {
    LATEST("최신순"),
}
