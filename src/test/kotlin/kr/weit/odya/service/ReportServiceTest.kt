package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.community.CommunityDeleteEvent
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.community.getImageNamesById
import kr.weit.odya.domain.communitycomment.CommunityCommentRepository
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.report.ReportCommunityRepository
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.report.existsByCommunityIdAndUserId
import kr.weit.odya.domain.report.existsByJournalIdAndUserId
import kr.weit.odya.domain.report.existsByReviewAndUserId
import kr.weit.odya.domain.traveljournal.TravelJournalDeleteEvent
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByContentImageNames
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.TEST_CONTENT_IMAGES
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
import kr.weit.odya.support.TEST_REPORT_ID
import kr.weit.odya.support.TEST_REPORT_OTHER_REASON
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createReportCommunity
import kr.weit.odya.support.createReportCommunityRequest
import kr.weit.odya.support.createReportPlaceReview
import kr.weit.odya.support.createReportPlaceReviewRequest
import kr.weit.odya.support.createReportTravelJournal
import kr.weit.odya.support.createReportTravelJournalRequest
import kr.weit.odya.support.createTravelJournal
import org.springframework.context.ApplicationEventPublisher

class ReportServiceTest : DescribeSpec(
    {
        val reportPlaceReviewRepository = mockk<ReportPlaceReviewRepository>()
        val userRepository = mockk<UserRepository>()
        val reportTravelJournalRepository = mockk<ReportTravelJournalRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val placeReviewRepository = mockk<PlaceReviewRepository>()
        val fileService = mockk<FileService>()
        val reportCommunityRepository = mockk<ReportCommunityRepository>()
        val communityRepository = mockk<CommunityRepository>()
        val communityCommentRepository = mockk<CommunityCommentRepository>()
        val eventPublisher = mockk<ApplicationEventPublisher>()
        val reportService = ReportService(
            communityRepository,
            reportPlaceReviewRepository,
            reportTravelJournalRepository,
            travelJournalRepository,
            userRepository,
            placeReviewRepository,
            reportCommunityRepository,
            communityCommentRepository,
            eventPublisher,
        )
        val user = createOtherUser()

        describe("reportPlaceReview 메소드") {
            val placeReview = createPlaceReview()
            val request = createReportPlaceReviewRequest()
            val placeReviewId = request.placeReviewId
            context("신고한 유저와 신고 사유가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_ID, request.reportReason)
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(placeReviewId) } returns 4
                it("신고가 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("신고한 유저와 기타 신고 사유가 전달될 경우") {
                val otherRequest = request.copy(reportReason = ReportReason.OTHER, otherReason = TEST_REPORT_OTHER_REASON)
                val otherId = otherRequest.placeReviewId
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(otherId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_ID, otherRequest.reportReason, otherRequest.otherReason)
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(otherId) } returns 4
                it("신고가 기타 신고 사유와 함께 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, otherRequest) }
                }
            }

            context("신고 등록 후 한줄 리뷰의 신고가 5회 이상일 경우") {
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_ID, request.reportReason, request.otherReason)
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(placeReviewId) } returns 5
                every { reportPlaceReviewRepository.deleteAllByPlaceReviewId(placeReviewId) } just runs
                every { placeReviewRepository.deleteById(placeReviewId) } just runs
                it("해당 한 줄 리뷰를 삭제한다.") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("작성자가 자신의 리뷰를 신고 등록을 요청한 경우") {
                val otherPlaceReview = createPlaceReview(user)
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns false
                every { placeReviewRepository.getByPlaceReviewId(placeReviewId) } returns otherPlaceReview
                it("[IllegalArgumentException]을 반환한다.") {
                    shouldThrow<IllegalArgumentException> { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("이미 신고한 리뷰를 신고 등록을 요청한 경우") {
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns true
                every { placeReviewRepository.getByPlaceReviewId(placeReviewId) } returns placeReview
                it("[ExistResourceException]을 반환한다.") {
                    shouldThrow<ExistResourceException> { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("존재하지 않는 유저ID가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("존재하지 않는 한줄 리뷰 ID가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByReviewAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(placeReviewId) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportPlaceReview(user.id, request) }
                }
            }
        }

        describe("reportTravelJournal 메소드") {
            val travelJournal = createTravelJournal()
            val request = createReportTravelJournalRequest()
            val travelJournalId = request.travelJournalId
            context("신고한 유저와 신고 사유가 전달될 경우") {
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.save(any()) } returns createReportTravelJournal(travelJournal, user)
                every { reportTravelJournalRepository.countAllByTravelJournalId(travelJournalId) } returns 4
                it("신고가 정상적으로 등록된다") {
                    shouldNotThrowAny { reportService.reportTravelJournal(user.id, request) }
                }
            }
            context("신고한 유저와 기타 신고 사유가 전달될 경우") {
                val otherRequest = request.copy(reportReason = ReportReason.OTHER, otherReason = TEST_REPORT_OTHER_REASON)
                val otherId = otherRequest.travelJournalId
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(otherId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(otherId) } returns travelJournal
                every { reportTravelJournalRepository.save(any()) } returns createReportTravelJournal(travelJournal, user, TEST_REPORT_ID, otherRequest.reportReason, otherRequest.otherReason)
                every { reportTravelJournalRepository.countAllByTravelJournalId(otherId) } returns 4
                it("신고가 기타 신고 사유와 함께 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("신고 등록 후 여행 일지의 신고가 5회 이상일 경우") {
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.save(any()) } returns createReportTravelJournal(travelJournal, user, TEST_REPORT_ID, request.reportReason, request.otherReason)
                every { reportTravelJournalRepository.countAllByTravelJournalId(travelJournalId) } returns 5
                every { reportTravelJournalRepository.deleteAllByTravelJournalId(travelJournalId) } just runs
                every { travelJournalRepository.deleteById(travelJournalId) } just runs
                every { travelJournalRepository.getByContentImageNames(travelJournalId) } returns TEST_CONTENT_IMAGES.map { it.name }
                every { communityRepository.updateTravelJournalIdToNull(travelJournalId) } just runs
                every { fileService.deleteFile(any()) } just runs
                every { eventPublisher.publishEvent(any<TravelJournalDeleteEvent>()) } just runs
                it("해당 여행 일지를 삭제한다.") {
                    shouldNotThrowAny { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("작성자가 자신의 여행 일지를 신고 등록을 요청한 경우") {
                val travelJournal2 = createTravelJournal(user = user)
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId, user.id) } returns false
                every { travelJournalRepository.getByTravelJournalId(travelJournalId) } returns travelJournal2
                it("[IllegalArgumentException]을 반환한다.") {
                    shouldThrow<IllegalArgumentException> { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("이미 신고한 여행 일지를 신고 등록을 요청한 경우") {
                every { travelJournalRepository.getByTravelJournalId(travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId, user.id) } returns true
                it("[ExistResourceException]을 반환한다.") {
                    shouldThrow<ExistResourceException> { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("존재하지 않는 유저ID가 전달될 경우") {
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("존재하지 않는 여행 일지 ID가 전달될 경우") {
                every { reportTravelJournalRepository.existsByJournalIdAndUserId(travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(travelJournalId) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportTravelJournal(user.id, request) }
                }
            }
        }

        describe("reportCommunity 메소드") {
            val community = createCommunity()
            val request = createReportCommunityRequest()
            val communityId = request.communityId
            context("신고한 유저와 신고 사유가 전달될 경우") {
                every { communityRepository.getByCommunityId(communityId) } returns community
                every { reportCommunityRepository.existsByCommunityIdAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { reportCommunityRepository.save(any()) } returns createReportCommunity(community, user)
                every { reportCommunityRepository.countAllByCommunityId(communityId) } returns 4
                it("신고가 정상적으로 등록된다") {
                    shouldNotThrowAny { reportService.reportCommunity(user.id, request) }
                }
            }
            context("신고한 유저와 기타 신고 사유가 전달될 경우") {
                val otherRequest = request.copy(reportReason = ReportReason.OTHER, otherReason = TEST_REPORT_OTHER_REASON)
                val otherId = request.communityId
                every { reportCommunityRepository.existsByCommunityIdAndUserId(otherId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { communityRepository.getByCommunityId(otherId) } returns community
                every { reportCommunityRepository.save(any()) } returns createReportCommunity(community, user, TEST_REPORT_ID, otherRequest.reportReason, otherRequest.otherReason)
                every { reportCommunityRepository.countAllByCommunityId(otherId) } returns 4
                it("신고가 기타 신고 사유와 함께 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportCommunity(user.id, request) }
                }
            }

            context("신고 등록 후 커뮤니티 글의 신고가 5회 이상일 경우") {
                every { reportCommunityRepository.existsByCommunityIdAndUserId(communityId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { communityRepository.getByCommunityId(communityId) } returns community
                every { reportCommunityRepository.save(any()) } returns createReportCommunity(community, user, TEST_REPORT_ID, request.reportReason, request.otherReason)
                every { reportCommunityRepository.countAllByCommunityId(communityId) } returns 5
                every { reportCommunityRepository.deleteAllByCommunityId(communityId) } just runs
                every { reportCommunityRepository.deleteAllByCommunityId(communityId) } just runs
                every { communityCommentRepository.deleteAllByCommunityId(communityId) } just runs
                every { communityRepository.getImageNamesById(communityId) } returns listOf(TEST_GENERATED_FILE_NAME)
                every { fileService.deleteFile(TEST_GENERATED_FILE_NAME) } just runs
                every { communityRepository.deleteById(communityId) } just runs
                every { eventPublisher.publishEvent(any<CommunityDeleteEvent>()) } just runs
                it("해당 커뮤니티 글을 삭제한다.") {
                    shouldNotThrowAny { reportService.reportCommunity(user.id, request) }
                }
            }

            context("작성자가 자신의 커뮤니티 글을 신고 등록을 요청한 경우") {
                val community2 = createCommunity(user = user)
                every { reportCommunityRepository.existsByCommunityIdAndUserId(communityId, user.id) } returns false
                every { communityRepository.getByCommunityId(communityId) } returns community2
                it("[IllegalArgumentException]을 반환한다.") {
                    shouldThrow<IllegalArgumentException> { reportService.reportCommunity(user.id, request) }
                }
            }

            context("이미 신고한 커뮤니티 글을 신고 등록을 요청한 경우") {
                every { communityRepository.getByCommunityId(communityId) } returns community
                every { reportCommunityRepository.existsByCommunityIdAndUserId(any(), user.id) } returns true
                it("[ExistResourceException]을 반환한다.") {
                    shouldThrow<ExistResourceException> { reportService.reportCommunity(user.id, request) }
                }
            }

            context("존재하지 않는 유저ID가 전달될 경우") {
                every { communityRepository.getByCommunityId(communityId) } returns community
                every { reportCommunityRepository.existsByCommunityIdAndUserId(any(), user.id) } returns false
                every { userRepository.getByUserId(user.id) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportCommunity(user.id, request) }
                }
            }

            context("존재하지 않는 커뮤니티 ID가 전달될 경우") {
                every { reportCommunityRepository.existsByCommunityIdAndUserId(communityId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { communityRepository.getByCommunityId(communityId) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportCommunity(user.id, request) }
                }
            }
        }
    },
)
