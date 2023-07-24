package kr.weit.odya.service.dto

import kr.weit.odya.domain.favoritePlace.FavoritePlace
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NullOrNotBlank

data class FavoritePlaceRequest(
    @field:NullOrNotBlank(message = "장소 ID는 빈 문자열이 될 수 없습니다.")
    val placeId: String,
) {
    fun toEntity(user: User): FavoritePlace = FavoritePlace(0L, placeId, user)
}

data class FavoritePlaceResponse(
    val id: Long,
    val placeId: String,
    val userId: Long,
) {
    constructor(favoritePlace: FavoritePlace) : this(
        favoritePlace.id,
        favoritePlace.placeId,
        favoritePlace.registrantsId,
    )
}

data class SliceFavoritePlaceResponse private constructor(
    override var hasNext: Boolean,
    override val content: List<FavoritePlaceResponse>,
) : SliceResponse<FavoritePlaceResponse>(hasNext, content) {
    constructor(size: Int, content: List<FavoritePlaceResponse>) : this(
        content.size > size,
        if (content.size > size) content.dropLast(1) else content,
    )
}
