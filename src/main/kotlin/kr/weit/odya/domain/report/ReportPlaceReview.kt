package kr.weit.odya.domain.report

import jakarta.persistence.Column
import jakarta.persistence.Embedded
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
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "report_place_review_unique",
            columnNames = ["user_id", "place_review_id"],
        ),
    ],
    indexes = [
        Index(name = "report_place_review_place_review_id_index", columnList = "place_review_id"),
    ],
)
@Entity
@SequenceGenerator(
    name = "REPORT_PLACE_REVIEW_SEQ_GENERATOR",
    sequenceName = "REPORT_PLACE_REVIEW_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class ReportPlaceReview(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_PLACE_REVIEW_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "place_review_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val placeReview: PlaceReview,

    @Embedded
    val commonReportInformation: CommonReportInformation,
) : BaseTimeEntity()
