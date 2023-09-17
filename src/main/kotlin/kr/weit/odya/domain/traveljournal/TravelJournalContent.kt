package kr.weit.odya.domain.traveljournal

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.support.domain.BaseModifiableEntity
import java.time.LocalDate

private const val MIN_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT = 1

@Table(
    indexes = [
        Index(name = "travel_journal_content_travel_journal_id_index", columnList = "travel_journal_id"),
        Index(name = "travel_journal_content_place_id_index", columnList = "placeId"),
    ],
)
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
        if (mutableTravelJournalContentImages.size - travelJournalContentImages.size < MIN_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT) {
            throw IllegalArgumentException("여행 일지 콘텐츠에는 최소 1개 이상의 이미지가 존재해야 합니다.")
        }
        mutableTravelJournalContentImages.removeAll(travelJournalContentImages)
    }

    fun addTravelJournalContentImages(travelJournalContentImages: List<TravelJournalContentImage>) {
        mutableTravelJournalContentImages.addAll(travelJournalContentImages)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other as? TravelJournalContent == null) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
