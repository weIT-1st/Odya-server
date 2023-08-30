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
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_REPORT_PLACE_REVIEW_ID
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createReportPlaceReview
import kr.weit.odya.support.createReportPlaceReviewRequest

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
            val reportReview = createReportPlaceReviewRequest()
            context("신고한 유저와 신고 사유가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(reportReview.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(reportReview.placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_PLACE_REVIEW_ID, reportReview.reportReason)
                it("신고가 정상적으로 등록된다") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, reportReview) }
                }
            }

            context("신고한 유저와 기타 신고 사유가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(reportReview.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(reportReview.placeReviewId) } returns placeReview
                every { reportPlaceReviewRepository.save(any()) } returns createReportPlaceReview(placeReview, user, TEST_REPORT_PLACE_REVIEW_ID, reportReview.reportReason, reportReview.otherReason)
                it("신고가 기타 신고 사유와 함께 정상적으로 등록된다") {
                    shouldNotThrowAny { reportService.reportPlaceReview(user.id, reportReview) }
                }
            }

            context("작성자가 자신의 리뷰를 신고 등록을 요청한 경우") {
                val placeReview2 = createPlaceReview(user)
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(reportReview.placeReviewId, user.id) } returns false
                every { placeReviewRepository.getByPlaceReviewId(reportReview.placeReviewId) } returns placeReview2
                it("[IllegalArgumentException]을 반환한다.") {
                    shouldThrow<IllegalArgumentException> { reportService.reportPlaceReview(user.id, reportReview) }
                }
            }

            context("이미 신고한 리뷰를 신고 등록을 요청한 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(reportReview.placeReviewId, user.id) } returns true
                every { placeReviewRepository.getByPlaceReviewId(reportReview.placeReviewId) } returns placeReview
                it("[ExistResourceException]을 반환한다.") {
                    shouldThrow<ExistResourceException> { reportService.reportPlaceReview(user.id, reportReview) }
                }
            }

            context("존재하지 않는 유저ID가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(reportReview.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportPlaceReview(user.id, reportReview) }
                }
            }

            context("존재하지 않는 한줄 리뷰 ID가 전달될 경우") {
                every { reportPlaceReviewRepository.existsByPlaceReviewIdAndUserId(reportReview.placeReviewId, user.id) } returns false
                every { userRepository.getByUserId(user.id) } returns user
                every { placeReviewRepository.getByPlaceReviewId(reportReview.placeReviewId) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("[NoSuchElementException]을 반환한다.") {
                    shouldThrow<NoSuchElementException> { reportService.reportPlaceReview(user.id, reportReview) }
                }
            }
        }

        describe("checkReportPlaceReviewCount 메소드") {
            context("신고 등록 후 한줄 리뷰의 신고가 5회 이상일 경우") {
                val reportReview = createReportPlaceReviewRequest()
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(reportReview.placeReviewId) } returns 5
                every { reportPlaceReviewRepository.deleteByPlaceReviewId(reportReview.placeReviewId) } just runs
                every { placeReviewRepository.deleteById(reportReview.placeReviewId) } just runs
                it("해당 한 줄 리뷰를 삭제한다.") {
                    shouldNotThrowAny { reportService.checkReportPlaceReviewCount(TEST_PLACE_REVIEW_ID) }
                }
            }

            context("신고 등록 후 한줄 리뷰의 신고가 5회 이하일 경우") {
                val reportReview = createReportPlaceReviewRequest()
                every { reportPlaceReviewRepository.countAllByPlaceReviewId(reportReview.placeReviewId) } returns 4
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny { reportService.checkReportPlaceReviewCount(TEST_PLACE_REVIEW_ID) }
                }
            }
        }
    },
)
