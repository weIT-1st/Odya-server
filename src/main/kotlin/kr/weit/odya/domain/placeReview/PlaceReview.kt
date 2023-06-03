package kr.weit.odya.domain.placeReview

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import kr.weit.odya.domain.BaseModifiableEntity
import kr.weit.odya.domain.user.User

@Entity
@Table(name = "place_review")
@IdClass(PlaceReviewId::class)
data class PlaceReview(
    @Id
    @Column(length = 400, nullable = false, updatable = false)
    val placeId: String,

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val user: User,

    @Column(nullable = false, updatable = false)
    val starRating: Int,

    @Column(length = 300, nullable = false, updatable = false)
    val comment: String
) : BaseModifiableEntity()
