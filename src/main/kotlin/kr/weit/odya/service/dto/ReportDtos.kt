package kr.weit.odya.service.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.report.ReportPlaceReview
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NullOrNotBlank
import org.hibernate.validator.constraints.Length

data class ReportReasonsResponse(val name: String, val reason: String)

data class ReportPlaceReviewRequest(
    @field:NotNull(message = "placeReviewId는 필수 입력값입니다.")
    @field:Positive(message = "placeReviewId는 양수여야 합니다.")
    val placeReviewId: Long,
    @field:NotNull(message = "신고 사유는 필수 입력값입니다.")
    val reportReason: ReportReason,
    @field:NullOrNotBlank(message = "기타 사유는 공백일 수 없습니다.")
    @field:Length(max = 60, message = "기타 사유는 20자 이내여야 합니다.")
    val otherReason: String? = null,
) {
    fun toEntity(user: User, placeReview: PlaceReview) =
        ReportPlaceReview(
            user = user,
            placeReview = placeReview,
            reportReason = reportReason,
            reason = otherReason,
        )
}

data class ReportTravelJournalRequest(
    @field:NotNull(message = "travelJournalId는 필수 입력값입니다.")
    @field:Positive(message = "travelJournalId는 양수여야 합니다.")
    val travelJournalId: Long,
    @field:NotNull(message = "신고 사유는 필수 입력값입니다.")
    val reportReason: ReportReason,
    @field:NullOrNotBlank(message = "기타 사유는 공백일 수 없습니다.")
    @field:Length(max = 60, message = "기타 사유는 20자 이내여야 합니다.")
    val otherReason: String? = null,
) {
    fun toEntity(user: User, travelJournal: TravelJournal) = ReportTravelJournal(
        user = user,
        travelJournal = travelJournal,
        reportReason = reportReason,
        reason = otherReason,
    )
}
