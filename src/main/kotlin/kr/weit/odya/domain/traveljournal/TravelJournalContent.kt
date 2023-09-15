package kr.weit.odya.domain.traveljournal

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
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

    travelJournalContentInformation: TravelJournalContentInformation,

    travelJournalContentImages: List<TravelJournalContentImage>,
) : BaseModifiableEntity() {
    var travelJournalContentInformation: TravelJournalContentInformation = travelJournalContentInformation
        protected set

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(
        name = "travel_journal_content_id",
        nullable = false,
        updatable = false,
        columnDefinition = "NUMERIC(19, 0)",
    )
    private val mutableTravelJournalContentImages: MutableList<TravelJournalContentImage> =
        travelJournalContentImages.toMutableList()
    val travelJournalContentImages: List<TravelJournalContentImage>
        get() = mutableTravelJournalContentImages

    val content: String?
        get() = travelJournalContentInformation.content

    val placeId: String?
        get() = travelJournalContentInformation.placeId

    val coordinates: Coordinates?
        get() = travelJournalContentInformation.coordinates

    val travelDate: LocalDate
        get() = travelJournalContentInformation.travelDate

    fun changeTravelJournalContent(travelJournalContentInformation: TravelJournalContentInformation) {
        this.travelJournalContentInformation = travelJournalContentInformation
    }

    fun deleteTravelJournalContentImages(travelJournalContentImages: List<TravelJournalContentImage>) {
        mutableTravelJournalContentImages.removeAll(travelJournalContentImages)
    }

    fun addTravelJournalContentImages(travelJournalContentImages: List<TravelJournalContentImage>) {
        mutableTravelJournalContentImages.addAll(travelJournalContentImages)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TravelJournalContent

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
