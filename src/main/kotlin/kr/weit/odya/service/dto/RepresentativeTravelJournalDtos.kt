package kr.weit.odya.service.dto

import com.fasterxml.jackson.annotation.JsonFormat
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournal
import java.time.LocalDate

data class RepTravelJournalSummaryResponse(
    val repTravelJournalId: Long,
    val travelJournalId: Long,
    val title: String,
    val content: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    val travelStartDate: LocalDate,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    val travelEndDate: LocalDate,
    val travelJournalMainImageUrl: String,
    val isBookmarked: Boolean,
    val writer: UserSimpleResponse,
    val travelCompanionSimpleResponses: List<TravelCompanionSimpleResponse>?,
) {
    companion object {
        fun from(
            repTravelJournal: RepresentativeTravelJournal,
            travelJournalMainImageUrl: String,
            isBookmarked: Boolean,
            userSimpleResponse: UserSimpleResponse,
            travelCompanionSimpleResponses: List<TravelCompanionSimpleResponse>?,
        ) =
            RepTravelJournalSummaryResponse(
                repTravelJournal.id,
                repTravelJournal.travelJournal.id,
                repTravelJournal.travelJournal.title,
                repTravelJournal.travelJournal.travelJournalContents[0].content,
                repTravelJournal.travelJournal.travelStartDate,
                repTravelJournal.travelJournal.travelEndDate,
                travelJournalMainImageUrl,
                isBookmarked,
                userSimpleResponse,
                travelCompanionSimpleResponses,
            )
    }
}
