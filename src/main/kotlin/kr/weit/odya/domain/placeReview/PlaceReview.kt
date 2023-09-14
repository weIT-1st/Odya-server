package kr.weit.odya.domain.placeReview

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
import kr.weit.odya.support.domain.BaseModifiableEntity

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "place_id_user_id_unique",
            columnNames = ["place_id", "user_id"],
        ),
    ],
    indexes = [
        Index(name = "place_review_user_id_index", columnList = "user_id"),
    ],
)
@SequenceGenerator(
    name = "PLACE_REVIEW_SEQ_GENERATOR",
    sequenceName = "PLACE_REVIEW_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Entity
class PlaceReview(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PLACE_REVIEW_SEQ_GENERATOR")
    val id: Long,

    @Column(name = "place_id", length = 400, nullable = false, updatable = false)
    val placeId: String,

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val user: User,

    @Column(nullable = false)
    var starRating: Int,

    @Column(length = 90, nullable = false)
    var review: String,
) : BaseModifiableEntity() {
    val writerId: Long
        get() = user.id

    val writerNickname: String
        get() = user.nickname
}
