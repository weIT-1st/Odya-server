package kr.weit.odya.service

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityDeleteEvent
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.community.getImageNamesById
import kr.weit.odya.domain.communitycomment.CommunityCommentRepository
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.report.ReportCommunityRepository
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.report.existsByCommunityIdAndUserId
import kr.weit.odya.domain.report.existsByJournalIdAndUserId
import kr.weit.odya.domain.report.existsByReviewAndUserId
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalDeleteEvent
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByContentImageNames
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.ReportCommunityRequest
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportTravelJournalRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val MAX_REPORTED_ACCUMULATION = 5

@Service
class ReportService(
    private val communityRepository: CommunityRepository,
    private val reportPlaceReviewRepository: ReportPlaceReviewRepository,
    private val reportTravelJournalRepository: ReportTravelJournalRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val userRepository: UserRepository,
    private val placeReviewRepository: PlaceReviewRepository,
    private val reportCommunityRepository: ReportCommunityRepository,
    private val communityCommentRepository: CommunityCommentRepository,
    private val eventPublisher: ApplicationEventPublisher,
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

    @Transactional
    fun reportCommunity(userId: Long, reportCommunityRequest: ReportCommunityRequest) {
        val communityId = reportCommunityRequest.communityId
        val community = communityRepository.getByCommunityId(communityId)
        verificationReportCommunity(userId, community, reportCommunityRequest)
        reportCommunityRepository.save(reportCommunityRequest.toEntity(userRepository.getByUserId(userId), community))
        checkReportCommunityCount(communityId)
    }

    private fun verificationReportReview(userId: Long, placeReview: PlaceReview, reportPlaceReviewRequest: ReportPlaceReviewRequest) {
        require(placeReview.writerId != userId) { "${placeReview.id} : 자신이 쓴 글은 신고할 수 없습니다." }
        if (reportPlaceReviewRepository.existsByReviewAndUserId(placeReview.id, userId)) {
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
        if (reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournal.id, userId)) {
            throw ExistResourceException("${travelJournal.id} : 이미 신고한 글입니다.")
        }
        require(reportTravelJournalRequest.reportReason != ReportReason.OTHER || reportTravelJournalRequest.otherReason != null) { "기타 사유는 필수 입력값입니다." }
    }

    private fun checkReportTravelJournalCount(travelJournalId: Long) {
        if (reportTravelJournalRepository.countAllByTravelJournalId(travelJournalId) >= MAX_REPORTED_ACCUMULATION) {
            reportTravelJournalRepository.deleteAllByTravelJournalId(travelJournalId)
            communityRepository.updateTravelJournalIdToNull(travelJournalId)
            travelJournalRepository.deleteById(travelJournalId)
            eventPublisher.publishEvent(TravelJournalDeleteEvent(travelJournalRepository.getByContentImageNames(travelJournalId)))
        }
    }

    private fun verificationReportCommunity(userId: Long, community: Community, reportCommunityRequest: ReportCommunityRequest) {
        require(community.user.id != userId) { "${community.id} : 자신이 쓴 글은 신고할 수 없습니다." }
        if (reportCommunityRepository.existsByCommunityIdAndUserId(community.id, userId)) {
            throw ExistResourceException("${community.id} : 이미 신고한 글입니다.")
        }
        require(reportCommunityRequest.reportReason != ReportReason.OTHER || reportCommunityRequest.otherReason != null) { "기타 사유는 필수 입력값입니다." }
    }
    private fun checkReportCommunityCount(communityId: Long) {
        if (reportCommunityRepository.countAllByCommunityId(communityId) >= 5) {
            reportCommunityRepository.deleteAllByCommunityId(communityId)
            communityCommentRepository.deleteAllByCommunityId(communityId)
            communityRepository.deleteById(communityId)
            eventPublisher.publishEvent(CommunityDeleteEvent(communityRepository.getImageNamesById(communityId)))
        }
    }
}
