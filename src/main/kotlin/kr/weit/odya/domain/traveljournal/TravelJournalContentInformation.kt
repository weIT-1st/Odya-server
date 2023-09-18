package kr.weit.odya.domain.traveljournal

import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import java.time.LocalDate

@Embeddable
data class TravelJournalContentInformation(
    @Column(length = 600)
    val content: String?,

    @Column(length = 400)
    val placeId: String?,

    @Embedded
    @AttributeOverride(name = "value", column = Column(name = "coordinates", nullable = false))
    val coordinates: Coordinates?,

    @Column(nullable = false)
    val travelDate: LocalDate,
)
