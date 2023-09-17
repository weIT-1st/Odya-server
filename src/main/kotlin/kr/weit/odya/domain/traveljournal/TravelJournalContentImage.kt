package kr.weit.odya.domain.traveljournal

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    indexes = [
        Index(name = "travel_journal_content_id_index", columnList = "travel_journal_content_id"),
        Index(name = "content_image_id_index", columnList = "content_image_id"),
    ],
)
@Entity
@SequenceGenerator(
    name = "TRAVEL_JOURNAL_CONTENT_IMAGE_SEQ_GENERATOR",
    sequenceName = "TRAVEL_JOURNAL_CONTENT_IMAGE_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class TravelJournalContentImage(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRAVEL_JOURNAL_CONTENT_IMAGE_SEQ_GENERATOR")
    val id: Long = 0L,

    @OneToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE])
    @JoinColumn(name = "content_image_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val contentImage: ContentImage,
) : BaseTimeEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TravelJournalContentImage

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
