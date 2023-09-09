package kr.weit.odya.domain.traveljournal

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseModifiableEntity
import java.time.LocalDate

@Table(
    indexes = [
        Index(name = "travel_journal_user_id_index", columnList = "user_id"),
    ],
)
@Entity
@SequenceGenerator(
    name = "TRAVEL_JOURNAL_SEQ_GENERATOR",
    sequenceName = "TRAVEL_JOURNAL_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class TravelJournal(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRAVEL_JOURNAL_SEQ_GENERATOR")
    val id: Long = 0L,

    @Column(nullable = false, length = 60)
    val title: String,

    @Column(nullable = false)
    val travelStartDate: LocalDate,

    @Column(nullable = false)
    val travelEndDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val visibility: TravelJournalVisibility,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User,

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "travel_journal_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val travelJournalContents: List<TravelJournalContent> = emptyList(),

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "travel_journal_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val travelCompanions: List<TravelCompanion>,
) : BaseModifiableEntity()
