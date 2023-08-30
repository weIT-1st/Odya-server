package kr.weit.odya.support

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.report.ReportPlaceReview
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportReasonsResponse
const val TEST_REPORT_PLACE_REVIEW_ID = 1L
const val TEST_REPORT_PLACE_REVIEW_ID_2 = 2L
const val TEST_REPORT_PLACE_REVIEW_ID_3 = 3L
const val TEST_REPORT_PLACE_REVIEW_OTHER_REASON = "기타 사유"
val TEST_REPORT_REASON = ReportReason.SPAM

fun createReportPlaceReview(
    placeReview: PlaceReview = createPlaceReview(),
    user: User = createUser(),
    id: Long = 0L,
    reportReason: ReportReason = TEST_REPORT_REASON,
    otherReason: String? = null,
) = ReportPlaceReview(id, user, placeReview, reportReason, otherReason)

fun createReportPlaceReviewRequest(placeReviewId: Long = TEST_PLACE_REVIEW_ID, reportReason: ReportReason = TEST_REPORT_REASON) = ReportPlaceReviewRequest(
    placeReviewId = placeReviewId,
    reportReason = reportReason,
)

fun createOtherReportPlaceReviewRequest(placeReviewId: Long = TEST_PLACE_REVIEW_ID, otherReason: String = TEST_REPORT_PLACE_REVIEW_OTHER_REASON) = ReportPlaceReviewRequest(
    placeReviewId = placeReviewId,
    reportReason = ReportReason.OTHER,
    otherReason = otherReason,
)

fun createReportReasonsResponse() = ReportReason.values().map { ReportReasonsResponse(it.name, it.reason) }
