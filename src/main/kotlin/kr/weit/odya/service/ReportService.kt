package kr.weit.odya.service

import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.report.countReportTravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportReasonsResponse
import kr.weit.odya.service.dto.ReportTravelJournalRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReportService(
    private val reportPlaceReviewRepository: ReportPlaceReviewRepository,
    private val reportTravelJournalRepository: ReportTravelJournalRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val userRepository: UserRepository,
    private val placeReviewRepository: PlaceReviewRepository,
) {
    fun getReportReasons() = ReportReason.values().map { ReportReasonsResponse(it.name, it.reason) }

    @Transactional
    fun reportPlaceReview(userId: Long, reportPlaceReviewRequest: ReportPlaceReviewRequest) {
        val placeReviewId = reportPlaceReviewRequest.placeReviewId
        val placeReview = placeReviewRepository.getByPlaceReviewId(placeReviewId)
        require(placeReview.writerId != userId) { "$placeReviewId : 자신이 쓴 글은 신고할 수 없습니다." }
        if (reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(placeReviewId, userId)) {
            throw ExistResourceException("$placeReviewId : 이미 신고한 리뷰입니다.")
        }
        reportPlaceReviewRepository.save(reportPlaceReviewRequest.toEntity(userRepository.getByUserId(userId), placeReview))
    }

    @Transactional
    fun checkReportPlaceReviewCount(placeReviewId: Long) {
        if (reportPlaceReviewRepository.countAllByPlaceReviewId(placeReviewId) == 5) {
            reportPlaceReviewRepository.deleteByPlaceReviewId(placeReviewId)
            placeReviewRepository.deleteById(placeReviewId)
        }
    }

    @Transactional
    fun reportTravelJournal(userId: Long, reportTravelJournalRequest: ReportTravelJournalRequest) {
        val travelJournalId = reportTravelJournalRequest.travelJournalId
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        require(travelJournal.user.id != userId) { "$travelJournalId : 자신이 쓴 글은 신고할 수 없습니다." }
        if (reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(travelJournalId, userId)) {
            throw ExistResourceException("$travelJournalId : 이미 신고한 여행 일지입니다.")
        }
        reportTravelJournalRepository.save(reportTravelJournalRequest.toEntity(userRepository.getByUserId(userId), travelJournalRepository.getByTravelJournalId(travelJournalId)))
        if (reportTravelJournalRepository.countReportTravelJournal(travelJournalId)) {
            travelJournalRepository.deleteById(travelJournalId)
        }
    }
}
