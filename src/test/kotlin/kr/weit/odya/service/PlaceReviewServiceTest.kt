package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_EXIST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createPlaceReviewRequest
import kr.weit.odya.support.createUser
import kr.weit.odya.support.updatePlaceReviewRequest

class PlaceReviewServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val placeReviewRepository = mockk<PlaceReviewRepository>()
        val sut = PlaceReviewService(placeReviewRepository, userRepository)
        describe("createPlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { placeReviewRepository.save(any()) } returns createPlaceReview()
                every { placeReviewRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns false
                it("리뷰를 생성한다.") {
                    shouldNotThrow<Exception> { sut.createReview(createPlaceReviewRequest(), TEST_USER_ID) }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.createReview(createPlaceReviewRequest(), TEST_USER_ID) }
                }
            }

            context("이미 리뷰를 작성한 장소인 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { placeReviewRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { sut.createReview(createPlaceReviewRequest(), TEST_USER_ID) }
                }
            }
        }

        describe("UpdatePlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_USER_ID) } returns createPlaceReview()
                every { placeReviewRepository.save(any()) } returns createPlaceReview()
                it("리뷰를 수정한다.") {
                    shouldNotThrow<Exception> { sut.updateReview(updatePlaceReviewRequest(), TEST_USER_ID) }
                }
            }

            context("존재하지 않는 장소리뷰ID인 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_EXIST_PLACE_REVIEW_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.updateReview(updatePlaceReviewRequest().copy(id = TEST_EXIST_PLACE_REVIEW_ID), TEST_USER_ID) }
                }
            }

            context("수정할 권한이 없는 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID) } returns createPlaceReview(createOtherUser())
                it("[ForbiddenException] 예외가 발생한다") {
                    shouldThrow<ForbiddenException> { sut.updateReview(updatePlaceReviewRequest(), TEST_USER_ID) }
                }
            }
        }

        describe("DeletePlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_USER_ID) } returns createPlaceReview()
                every { placeReviewRepository.delete(any()) } just Runs
                it("리뷰를 삭제한다.") {
                    shouldNotThrow<Exception> { sut.deleteReview(TEST_PLACE_REVIEW_ID, TEST_USER_ID) }
                }
            }

            context("존재하지 않는 장소리뷰ID인 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_EXIST_PLACE_REVIEW_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.deleteReview(TEST_EXIST_PLACE_REVIEW_ID, TEST_USER_ID) }
                }
            }

            context("삭제할 권한이 없는 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID) } returns createPlaceReview(createOtherUser())
                it("[ForbiddenException] 예외가 발생한다") {
                    shouldThrow<ForbiddenException> { sut.deleteReview(TEST_PLACE_REVIEW_ID, TEST_USER_ID) }
                }
            }
        }
    }
)
