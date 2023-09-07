package kr.weit.odya.support

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.report.ReportCommunity
import kr.weit.odya.domain.report.CommonReportInformation
import kr.weit.odya.domain.report.ReportPlaceReview
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ReportCommunityRequest
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportTravelJournalRequest

const val TEST_REPORT_ID = 1L
const val TEST_REPORT_OTHER_REASON = "기타 사유"
val TEST_REPORT_REASON = ReportReason.SPAM

fun createReportPlaceReview(
    placeReview: PlaceReview = createPlaceReview(),
    user: User = createUser(),
    id: Long = 0L,
    reportReason: ReportReason = TEST_REPORT_REASON,
    otherReason: String? = null,
) = ReportPlaceReview(
    id,
    placeReview,
    CommonReportInformation(user, reportReason, otherReason),
)

fun createReportPlaceReviewRequest(placeReviewId: Long = TEST_PLACE_REVIEW_ID, reportReason: ReportReason = TEST_REPORT_REASON, otherReason: String? = null) =
    ReportPlaceReviewRequest(placeReviewId, reportReason, otherReason)

fun createReportTravelJournal(
    travelJournal: TravelJournal = createTravelJournal(),
    user: User = createUser(),
    id: Long = 0L,
    reportReason: ReportReason = TEST_REPORT_REASON,
    otherReason: String? = null,
) = ReportTravelJournal(
    id,
    travelJournal,
    CommonReportInformation(user, reportReason, otherReason),
)

fun createReportTravelJournalRequest(travelJournalId: Long = TEST_TRAVEL_JOURNAL_ID, reportReason: ReportReason = TEST_REPORT_REASON)
= ReportTravelJournalRequest(travelJournalId, reportReason)

fun createReportCommunity(
    community: Community = createCommunity(),
    user: User = createUser(),
    id: Long = 0L,
    reportReason: ReportReason = TEST_REPORT_REASON,
    otherReason: String? = null,
) = ReportCommunity(id, user, community, reportReason, otherReason)

fun createReportCommunityRequest(
    communityId: Long = TEST_TRAVEL_JOURNAL_ID,
    reportReason: ReportReason = TEST_REPORT_REASON,
) = ReportCommunityRequest(communityId, reportReason)
