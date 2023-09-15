package kr.weit.odya.domain.traveljournal

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDate

@Embeddable
data class TravelJournalInformation(
    @Column(nullable = false, length = 60)
    val title: String,

    @Column(nullable = false)
    val travelStartDate: LocalDate,

    @Column(nullable = false)
    val travelEndDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val visibility: TravelJournalVisibility,
)
