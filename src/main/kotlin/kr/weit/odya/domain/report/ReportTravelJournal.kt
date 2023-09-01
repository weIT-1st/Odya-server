package kr.weit.odya.domain.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "report_travel_journal_unique",
            columnNames = ["travel_journal_id", "user_id"],
        ),
    ],
)
@Entity
@SequenceGenerator(
    name = "REPORT_TRAVEL_JOURNAL_SEQ_GENERATOR",
    sequenceName = "REPORT_TRAVEL_JOURNAL_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class ReportTravelJournal(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_TRAVEL_JOURNAL_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "travel_journal_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val travelJournal: TravelJournal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    val reportReason: ReportReason,

    reason: String? = null,
) : BaseTimeEntity() {
    @Column(updatable = false, length = 60)
    val otherReason = if (reportReason == ReportReason.OTHER) null else reason
}
