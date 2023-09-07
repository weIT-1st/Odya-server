package kr.weit.odya.service

import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByContentImageNames
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportTravelJournalRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val MAX_REPORTED_ACCUMULATION = 5

@Service
class ReportService(
    private val reportPlaceReviewRepository: ReportPlaceReviewRepository,
    private val reportTravelJournalRepository: ReportTravelJournalRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val userRepository: UserRepository,
    private val placeReviewRepository: PlaceReviewRepository,
    private val fileService: FileService,
) {
    @Transactional
    fun reportPlaceReview(userId: Long, reportPlaceReviewRequest: ReportPlaceReviewRequest) {
        val placeReviewId = reportPlaceReviewRequest.placeReviewId
        val placeReview = placeReviewRepository.getByPlaceReviewId(placeReviewId)
        verificationReportReview(userId, placeReview, reportPlaceReviewRequest)
        reportPlaceReviewRepository.save(reportPlaceReviewRequest.toEntity(userRepository.getByUserId(userId), placeReview))
        checkReportPlaceReviewCount(placeReviewId)
    }

    @Transactional
    fun reportTravelJournal(userId: Long, reportTravelJournalRequest: ReportTravelJournalRequest) {
        val travelJournalId = reportTravelJournalRequest.travelJournalId
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        verificationReportTravelJournal(userId, travelJournal, reportTravelJournalRequest)
        reportTravelJournalRepository.save(reportTravelJournalRequest.toEntity(userRepository.getByUserId(userId), travelJournal))
        checkReportTravelJournalCount(travelJournalId)
    }

    private fun verificationReportReview(userId: Long, placeReview: PlaceReview, reportPlaceReviewRequest: ReportPlaceReviewRequest) {
        require(placeReview.writerId != userId) { "${placeReview.id} : 자신이 쓴 글은 신고할 수 없습니다." }
        if (reportPlaceReviewRepository.existsByPlaceReviewIdAndCommonReportInformationUserId(placeReview.id, userId)) {
            throw ExistResourceException("${placeReview.id} : 이미 신고한 글입니다.")
        }
        require(reportPlaceReviewRequest.reportReason != ReportReason.OTHER || reportPlaceReviewRequest.otherReason != null) { "기타 사유는 필수 입력값입니다." }
    }

    private fun checkReportPlaceReviewCount(placeReviewId: Long) {
        if (reportPlaceReviewRepository.countAllByPlaceReviewId(placeReviewId) >= MAX_REPORTED_ACCUMULATION) {
            reportPlaceReviewRepository.deleteAllByPlaceReviewId(placeReviewId)
            placeReviewRepository.deleteById(placeReviewId)
        }
    }

    private fun verificationReportTravelJournal(userId: Long, travelJournal: TravelJournal, reportTravelJournalRequest: ReportTravelJournalRequest) {
        require(travelJournal.user.id != userId) { "${travelJournal.id} : 자신이 쓴 글은 신고할 수 없습니다." }
        if (reportTravelJournalRepository.existsByTravelJournalIdAndCommonReportInformationUserId(travelJournal.id, userId)) {
            throw ExistResourceException("${travelJournal.id} : 이미 신고한 글입니다.")
        }
        require(reportTravelJournalRequest.reportReason != ReportReason.OTHER || reportTravelJournalRequest.otherReason != null) { "기타 사유는 필수 입력값입니다." }
    }

    private fun checkReportTravelJournalCount(travelJournalId: Long) {
        if (reportTravelJournalRepository.countAllByTravelJournalId(travelJournalId) >= MAX_REPORTED_ACCUMULATION) {
            reportTravelJournalRepository.deleteAllByTravelJournalId(travelJournalId)
            travelJournalRepository.getByContentImageNames(travelJournalId).map { fileService.deleteFile(it) }
            travelJournalRepository.deleteById(travelJournalId)
        }
    }
}
