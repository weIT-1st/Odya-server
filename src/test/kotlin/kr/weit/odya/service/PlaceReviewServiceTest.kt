package kr.weit.odya.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.repository.PlaceReviewRepository
import kr.weit.odya.repository.UserRepository
import kr.weit.odya.service.dto.PlaceReviewDto
import kr.weit.odya.support.test.BaseTests.SpringTestEnvironment

@SpringTestEnvironment
class PlaceReviewServiceTest(
    private val sut: PlaceReviewService,
    private val userRepository: UserRepository,
    private val placeReviewRepository: PlaceReviewRepository
) : DescribeSpec(
    {
        val user = userRepository.findById(1L).get()
        describe("createPlaceReview 메소드") {
            context("유효한 데이터가 전달되면") {
                it("리뷰를 생성한다.") {
                    val placeReview = PlaceReviewDto(
                        placeId = "testPlaceId",
                        comment = "리뷰 내용",
                        rating = 5
                    )
                    sut.createReview(placeReview, user)

                    placeReviewRepository.findAllByPlaceId("testPlaceId").size shouldBe 1
                }
            }
        }
    }
)
