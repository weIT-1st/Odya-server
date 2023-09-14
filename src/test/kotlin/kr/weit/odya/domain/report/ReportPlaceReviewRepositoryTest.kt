package kr.weit.odya.domain.report

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.placeReview.PlaceReview
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createLowestRatingPlaceReview
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createReportPlaceReview
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class ReportPlaceReviewRepositoryTest(
    private val reportPlaceReviewRepository: ReportPlaceReviewRepository,
    private val placeReviewRepository: PlaceReviewRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var user3: User
        lateinit var placeReview: PlaceReview
        lateinit var placeReview2: PlaceReview
        lateinit var reportPlaceReview1: ReportPlaceReview
        lateinit var reportPlaceReview2: ReportPlaceReview
        lateinit var reportPlaceReview3: ReportPlaceReview
        lateinit var reportPlaceReview4: ReportPlaceReview
        beforeEach {
            user1 = userRepository.save(createUser())
            user2 = userRepository.save(createOtherUser())
            user3 = userRepository.save(createCustomUser("test_user3", "test_user3"))
            placeReview = placeReviewRepository.save(createPlaceReview(user1))
            placeReview2 = placeReviewRepository.save(createLowestRatingPlaceReview(user2))
            reportPlaceReview1 = reportPlaceReviewRepository.save(createReportPlaceReview(placeReview, user1))
            reportPlaceReview2 = reportPlaceReviewRepository.save(createReportPlaceReview(placeReview, user2))
            reportPlaceReview3 = reportPlaceReviewRepository.save(createReportPlaceReview(placeReview2, user1))
            reportPlaceReview4 = reportPlaceReviewRepository.save(createReportPlaceReview(placeReview2, user3))
        }

        context("한줄 리뷰 신고 수 조회") {
            expect("PLACE_REVIEW_ID와 일치하는 한줄 리뷰의 신고 수를 조회한다") {
                val result = reportPlaceReviewRepository.countAllByPlaceReviewId(placeReview.id)
                result shouldBe 2
            }
        }

        context("한줄 리뷰 신고 여부 확인") {
            expect("PLACE REVIEW ID와 USER ID가 일치하는 한줄 리뷰의 신고 여부를 확인한다(존재)") {
                val result = reportPlaceReviewRepository.existsByReviewAndUserId(placeReview.id, user1.id)
                result shouldBe true
            }

            expect("PLACE REVIEW ID와 USER ID가 일치하는 한줄 리뷰의 신고 여부를 확인한다(존재하지 않음)") {
                val result = reportPlaceReviewRepository.existsByReviewAndUserId(placeReview.id, user3.id)
                result shouldBe false
            }
        }

        context("한줄 리뷰 신고 수 삭제") {
            expect("PLACE_REVIEW_ID와 일치하는 한줄 리뷰의 신고 모두 삭제한다") {
                reportPlaceReviewRepository.deleteAllByPlaceReviewId(placeReview.id)
                reportPlaceReviewRepository.count() shouldBe 2
            }

            expect("USER_ID와 일치하는 한줄 리뷰의 신고 모두 삭제한다") {
                reportPlaceReviewRepository.deleteAllByUserId(user1.id)
                reportPlaceReviewRepository.count() shouldBe 2
            }
        }
    },
)
