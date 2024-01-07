package kr.weit.odya.domain.traveljournalbookmark

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Entity
@Table(
    indexes = [
        Index(name = "representative_travel_journal_travel_journal_id_index", columnList = "travel_journal_id"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "representative_travel_journal_unique",
            columnNames = ["user_id", "travel_journal_id"],
        ),
    ],
)
@SequenceGenerator(
    name = "REPRESENTATIVE_TRAVEL_JOURNAL_SEQ_GENERATOR",
    sequenceName = "REPRESENTATIVE_TRAVEL_JOURNAL_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class RepresentativeTravelJournal(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPRESENTATIVE_TRAVEL_JOURNAL_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_journal_id", nullable = false, updatable = false)
    val travelJournal: TravelJournal,
) : BaseTimeEntity()
