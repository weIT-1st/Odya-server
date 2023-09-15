package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByPlaceReviewId
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_AVERAGE_RATING
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_NOT_EXIST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_REVIEW_COUNT
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_PROFILE_URL
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.creatSlicePlaceReviewResponse
import kr.weit.odya.support.createCountPlaceReviewResponse
import kr.weit.odya.support.createExistReviewResponse
import kr.weit.odya.support.createMockPlaceReview
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createPlaceReviewRequest
import kr.weit.odya.support.createUser
import kr.weit.odya.support.updatePlaceReviewRequest

class PlaceReviewServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val placeReviewRepository = mockk<PlaceReviewRepository>()
        val reportPlaceReviewRepository = mockk<ReportPlaceReviewRepository>()
        val fileService = mockk<FileService>()
        val sut = PlaceReviewService(placeReviewRepository, userRepository, reportPlaceReviewRepository, fileService)
        val user = createUser()

        describe("createPlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { placeReviewRepository.save(any()) } returns createPlaceReview(user)
                every { placeReviewRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns false
                it("리뷰를 생성한다.") {
                    shouldNotThrowAny { sut.createReview(createPlaceReviewRequest(), TEST_USER_ID) }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.createReview(createPlaceReviewRequest(), TEST_USER_ID) }
                }
            }

            context("이미 리뷰를 작성한 장소인 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { placeReviewRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { sut.createReview(createPlaceReviewRequest(), TEST_USER_ID) }
                }
            }
        }

        describe("UpdatePlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_USER_ID) } returns createPlaceReview(user)
                every { placeReviewRepository.save(any()) } returns createPlaceReview(user)
                it("리뷰를 수정한다.") {
                    shouldNotThrowAny { sut.updateReview(updatePlaceReviewRequest(), TEST_USER_ID) }
                }
            }

            context("존재하지 않는 장소리뷰ID인 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_NOT_EXIST_PLACE_REVIEW_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> {
                        sut.updateReview(
                            updatePlaceReviewRequest().copy(id = TEST_NOT_EXIST_PLACE_REVIEW_ID),
                            TEST_USER_ID,
                        )
                    }
                }
            }

            context("수정할 권한이 없는 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID) } returns createPlaceReview(
                    createOtherUser(),
                )
                it("[ForbiddenException] 예외가 발생한다") {
                    shouldThrow<ForbiddenException> { sut.updateReview(updatePlaceReviewRequest(), TEST_USER_ID) }
                }
            }
        }

        describe("DeletePlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_USER_ID) } returns createPlaceReview(user)
                every { placeReviewRepository.deleteById(any()) } just Runs
                every { reportPlaceReviewRepository.deleteAllByPlaceReviewId(any()) } just Runs
                it("리뷰를 삭제한다.") {
                    shouldNotThrowAny { sut.deleteReview(TEST_PLACE_REVIEW_ID, TEST_USER_ID) }
                }
            }

            context("존재하지 않는 장소리뷰ID인 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_NOT_EXIST_PLACE_REVIEW_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.deleteReview(TEST_NOT_EXIST_PLACE_REVIEW_ID, TEST_USER_ID) }
                }
            }

            context("삭제할 권한이 없는 경우") {
                every { placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID) } returns createPlaceReview(createOtherUser())
                it("[ForbiddenException] 예외가 발생한다") {
                    shouldThrow<ForbiddenException> { sut.deleteReview(TEST_PLACE_REVIEW_ID, TEST_USER_ID) }
                }
            }
        }

        describe("getByPlaceReviewList 메소드") {
            context("유효한 placeId가 전달되면") {
                every { placeReviewRepository.findSliceByPlaceIdOrderBySortType(TEST_PLACE_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) } returns listOf(createMockPlaceReview(user))
                every { placeReviewRepository.getAverageRatingByPlaceId(TEST_PLACE_ID) } returns TEST_AVERAGE_RATING
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("리뷰를 조회한다.") {
                    sut.getByPlaceReviewList(TEST_PLACE_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) shouldBe creatSlicePlaceReviewResponse()
                }
            }
        }

        describe("getByUserReviewList 메소드") {
            context("유효한 userId가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { placeReviewRepository.findSliceByUserOrderBySortType(any(), TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) } returns listOf(createMockPlaceReview(user))
                every { placeReviewRepository.getAverageRatingByUser(user) } returns TEST_AVERAGE_RATING
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("리뷰를 조회한다.") {
                    sut.getByUserReviewList(TEST_USER_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) shouldBe creatSlicePlaceReviewResponse()
                }
            }

            context("존재하지 않는 userId가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.getByUserReviewList(TEST_USER_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) }
                }
            }
        }

        describe("getExistReview 메소드") {
            context("이미 리뷰를 쓴 userId와 placeId가 전달되면") {
                every { placeReviewRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("true를 반환한다.") {
                    sut.getExistReview(TEST_USER_ID, TEST_PLACE_ID) shouldBe createExistReviewResponse()
                }
            }

            context("리뷰를 쓰지 않은 userId와 placeId가 전달되면") {
                every { placeReviewRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns false
                it("false를 반환한다.") {
                    sut.getExistReview(TEST_USER_ID, TEST_PLACE_ID) shouldBe createExistReviewResponse(false)
                }
            }
        }

        describe("getReviewCount 메소드") {
            context("placeId가 전달되면") {
                every { placeReviewRepository.countByPlaceId(TEST_PLACE_ID) } returns TEST_PLACE_REVIEW_COUNT
                it("해당 장소의 한줄 리뷰 수를 반환") {
                    sut.getReviewCount(TEST_PLACE_ID) shouldBe createCountPlaceReviewResponse()
                }
            }
        }

        describe("deleteReviewRelatedData 메소드") {
            context("userId가 전달되면") {
                every { reportPlaceReviewRepository.deleteAllByCommonReportInformationUserId(TEST_USER_ID) } just runs
                every { placeReviewRepository.deleteByUserId(TEST_USER_ID) } just runs
                it("유저의 한 줄 리뷰 관련된 데이터 전부 삭제") {
                    shouldNotThrowAny { sut.deleteReviewRelatedData(TEST_USER_ID) }
                }
            }
        }
    },
)
