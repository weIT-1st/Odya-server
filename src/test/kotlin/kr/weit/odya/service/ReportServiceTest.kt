package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.TEST_REPORT_ID
import kr.weit.odya.support.TEST_REPORT_OTHER_REASON
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_END_DATE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_START_DATE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_TITLE
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createReportPlaceReview
import kr.weit.odya.support.createReportPlaceReviewRequest
import kr.weit.odya.support.createReportTravelJournal
import kr.weit.odya.support.createReportTravelJournalRequest
import kr.weit.odya.support.createTravelJournal

class ReportServiceTest : DescribeSpec(
    {
        val reportPlaceReviewRepository = mockk<ReportPlaceReviewRepository>()
        val userRepository = mockk<UserRepository>()
        val reportTravelJournalRepository = mockk<ReportTravelJournalRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val placeReviewRepository = mockk<PlaceReviewRepository>()
        val reportService = ReportService(reportPlaceReviewRepository, reportTravelJournalRepository, travelJournalRepository, userRepository, placeReviewRepository)
        val user = createOtherUser()

        describe("getReportReasons 메소드") {
            context("신고 사유가 정상적으로 있을 경우") {
                it("신고 사유 리스트가 반환된다") {
                    shouldNotThrowAny { reportService.getReportReasons() }
                }
            }
        }

        describe("reportPlaceReview 메소드") {
            val placeReview = createPlaceReview()
            val request = createReportPlaceReviewRequest()
            context("신고한 유저와 신고 사유가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(request.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(request.placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_ID, request.reportReason)
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(request.placeReviewId) } returns 4
                it("신고가 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("신고한 유저와 기타 신고 사유가 전달될 경우") {
                val otherRequest = request.copy(reportReason = ReportReason.OTHER, otherReason = TEST_REPORT_OTHER_REASON)
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(otherRequest.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(otherRequest.placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_ID, otherRequest.reportReason, otherRequest.otherReason)
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(otherRequest.placeReviewId) } returns 4
                it("신고가 기타 신고 사유와 함께 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, otherRequest) }
                }
            }

            context("신고 등록 후 한줄 리뷰의 신고가 5회 이상일 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(request.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(request.placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_ID, request.reportReason, request.otherReason)
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(request.placeReviewId) } returns 5
                every { reportPlaceReviewRepository.deleteByPlaceReviewId(request.placeReviewId) } just runs
                every { placeReviewRepository.deleteById(request.placeReviewId) } just runs
                it("해당 한 줄 리뷰를 삭제한다.") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("작성자가 자신의 리뷰를 신고 등록을 요청한 경우") {
                val otherPlaceReview = createPlaceReview(user)
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(request.placeReviewId, user.id) } returns false
                every { placeReviewRepository.getByPlaceReviewId(request.placeReviewId) } returns otherPlaceReview
                it("[IllegalArgumentException]을 반환한다.") {
                    shouldThrow<IllegalArgumentException> { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("이미 신고한 리뷰를 신고 등록을 요청한 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(request.placeReviewId, user.id) } returns true
                every { placeReviewRepository.getByPlaceReviewId(request.placeReviewId) } returns placeReview
                it("[ExistResourceException]을 반환한다.") {
                    shouldThrow<ExistResourceException> { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("존재하지 않는 유저ID가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(request.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportPlaceReview(user.id, request) }
                }
            }

            context("존재하지 않는 한줄 리뷰 ID가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(request.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(request.placeReviewId) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportPlaceReview(user.id, request) }
                }
            }
        }

        describe("reportTravelJournal 메소드") {
            val travelJournal = createTravelJournal()
            val request = createReportTravelJournalRequest()
            context("신고한 유저와 신고 사유가 전달될 경우") {
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(request.travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(request.travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.save(any()) } returns createReportTravelJournal(travelJournal, user)
                every { reportTravelJournalRepository.countAllByTravelJournalId(request.travelJournalId) } returns 4
                it("신고가 정상적으로 등록된다") {
                    shouldNotThrowAny { reportService.reportTravelJournal(user.id, request) }
                }
            }
            context("신고한 유저와 기타 신고 사유가 전달될 경우") {
                val otherRequest = request.copy(reportReason = ReportReason.OTHER, otherReason = TEST_REPORT_OTHER_REASON)
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(otherRequest.travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(otherRequest.travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.save(any()) } returns createReportTravelJournal(travelJournal, user, TEST_REPORT_ID, otherRequest.reportReason, otherRequest.otherReason)
                every { reportTravelJournalRepository.countAllByTravelJournalId(request.travelJournalId) } returns 4
                it("신고가 기타 신고 사유와 함께 정상적으로 등록된다.(신고 5회 미만)") {
                    shouldNotThrowAny { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("신고 등록 후 한줄 리뷰의 신고가 5회 이상일 경우") {
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(request.travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(request.travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.save(any()) } returns createReportTravelJournal(travelJournal, user, TEST_REPORT_ID, request.reportReason, request.otherReason)
                every { reportTravelJournalRepository.countAllByTravelJournalId(request.travelJournalId) } returns 5
                every { reportTravelJournalRepository.deleteAllByTravelJournalId(request.travelJournalId) } just runs
                every { travelJournalRepository.deleteById(request.travelJournalId) } just runs
                it("해당 한 줄 리뷰를 삭제한다.") {
                    shouldNotThrowAny { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("작성자가 자신의 리뷰를 신고 등록을 요청한 경우") {
                val travelJournal2 = createTravelJournal(TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_TITLE, TEST_TRAVEL_JOURNAL_START_DATE, TEST_TRAVEL_JOURNAL_END_DATE, TravelJournalVisibility.PUBLIC, user)
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(request.travelJournalId, user.id) } returns false
                every { travelJournalRepository.getByTravelJournalId(request.travelJournalId) } returns travelJournal2
                it("[IllegalArgumentException]을 반환한다.") {
                    shouldThrow<IllegalArgumentException> { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("이미 신고한 리뷰를 신고 등록을 요청한 경우") {
                every { travelJournalRepository.getByTravelJournalId(request.travelJournalId) } returns travelJournal
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(request.travelJournalId, user.id) } returns true
                it("[ExistResourceException]을 반환한다.") {
                    shouldThrow<ExistResourceException> { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("존재하지 않는 유저ID가 전달될 경우") {
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(request.travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportTravelJournal(user.id, request) }
                }
            }

            context("존재하지 않는 한줄 리뷰 ID가 전달될 경우") {
                every { reportTravelJournalRepository.existsByTravelJournalIdAndUserId(request.travelJournalId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { travelJournalRepository.getByTravelJournalId(request.travelJournalId) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportTravelJournal(user.id, request) }
                }
            }
        }
    },
)
