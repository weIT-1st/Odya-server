package kr.weit.odya.domain.favoritePlace

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "favorite_place_id_user_id_unique",
            columnNames = ["place_id", "user_id"],
        ),
    ],
    indexes = [
        Index(name = "favorite_place_id_index", columnList = "place_id"),
    ],
)
@SequenceGenerator(
    name = "FAVORITE_PLACE_SEQ_GENERATOR",
    sequenceName = "FAVORITE_PLACE_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Entity
class FavoritePlace(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FAVORITE_PLACE_SEQ_GENERATOR")
    val id: Long,

    @Column(name = "place_id", length = 400, nullable = false, updatable = false)
    val placeId: String,

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val user: User,
) : BaseTimeEntity() {
    val registrantsId: Long
        get() = user.id
}
