package kr.weit.odya.domain.traveljournal

import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import kr.weit.odya.support.domain.BaseModifiableEntity
import java.time.LocalDate

@Entity
@SequenceGenerator(
    name = "TRAVEL_JOURNAL_CONTENT_SEQ_GENERATOR",
    sequenceName = "TRAVEL_JOURNAL_CONTENT_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class TravelJournalContent(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRAVEL_JOURNAL_CONTENT_SEQ_GENERATOR")
    val id: Long = 0L,

    @Column(length = 600)
    val content: String?,

    @Column(length = 400)
    val placeId: String?,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "coordinates", nullable = false))
    val coordinates: Coordinates?,

    @Column(nullable = false)
    val travelDate: LocalDate,

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "travel_journal_content_id",
        nullable = false,
        updatable = false,
        columnDefinition = "NUMERIC(19, 0)",
    )
    val travelJournalContentImages: List<TravelJournalContentImage>,
) : BaseModifiableEntity()
