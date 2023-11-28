package kr.weit.odya.service.dto

import com.fasterxml.jackson.annotation.JsonFormat
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmark
import kr.weit.odya.domain.user.User
import java.time.LocalDate

data class TravelJournalBookmarkSummaryResponse(
    val travelJournalBookmarkId: Long,
    val travelJournalId: Long,
    val title: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd")
    val travelStartDate: LocalDate,
    val travelJournalMainImageUrl: String,
    val writer: UserSimpleResponse,
) {
    companion object {
        fun from(
            travelJournalBookmark: TravelJournalBookmark,
            profileUrl: String,
            travelJournalMainImageUrl: String,
            user: User,
            isFollowing: Boolean,
        ) =
            TravelJournalBookmarkSummaryResponse(
                travelJournalBookmark.id,
                travelJournalBookmark.travelJournal.id,
                travelJournalBookmark.travelJournal.title,
                travelJournalBookmark.travelJournal.travelStartDate,
                travelJournalMainImageUrl,
                UserSimpleResponse(user, profileUrl, isFollowing),
            )
    }
}
