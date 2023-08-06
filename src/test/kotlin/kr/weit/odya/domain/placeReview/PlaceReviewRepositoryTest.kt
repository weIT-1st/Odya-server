package kr.weit.odya.domain.placeReview

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_AVERAGE_RATING
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_OTHER_PLACE_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.createLatestReview
import kr.weit.odya.support.createLowestRatingPlaceReview
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class PlaceReviewRepositoryTest(
    private val placeReviewRepository: PlaceReviewRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
        lateinit var user1: User
        lateinit var user2: User
        lateinit var placeReview1: PlaceReview
        lateinit var placeReview2: PlaceReview
        lateinit var placeReview3: PlaceReview
        beforeEach {
            user1 = userRepository.save(createUser())
            user2 = userRepository.save(createOtherUser())
            placeReview1 = placeReviewRepository.save(createPlaceReview(user1))
            placeReview2 = placeReviewRepository.save(createLowestRatingPlaceReview(user2))
            placeReview3 = placeReviewRepository.save(createLatestReview(user1))
        }

        context("장소 리뷰 조회") {
            expect("PLACE_REVIEW_ID와 일치하는 장소 리뷰를 조회한다") {
                val result = placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID)
                result.id shouldBe TEST_PLACE_REVIEW_ID
            }

            expect("PLACE_ID와 일치하는 장소의 평균 별점을 조회한다") {
                val result = placeReviewRepository.getAverageRatingByPlaceId(TEST_PLACE_ID)
                result shouldBe TEST_AVERAGE_RATING
            }

            expect("USER와 일치하는 장소의 평균 별점을 조회한다") {
                val result = placeReviewRepository.getAverageRatingByUser(user1)
                result shouldBe TEST_AVERAGE_RATING
            }

            expect("PLACE_ID와 일치하는 lastId 초과의 id인 장소 리뷰를 조회한다") {
                val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID)
                result.first().placeId shouldBe TEST_PLACE_ID
                result.first().id shouldBeLessThan TEST_LAST_ID
            }

            expect("PLACE_ID와 일치하는 장소 리뷰를 size만큼 조회한다") {
                val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, null)
                result.size shouldBe TEST_SIZE + 1
                result.first().placeId shouldBe TEST_PLACE_ID
            }

            expect("USER와 일치하는 lastId 초과의 id인 조회한다") {
                val result = placeReviewRepository.getPlaceReviewListByUser(user1, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID)
                result.first().writerId shouldBe user1.id
                result.first().id shouldBeLessThan TEST_LAST_ID
            }

            expect("USER와 일치하는 장소 리뷰를 조회한다") {
                val result = placeReviewRepository.getPlaceReviewListByUser(user1, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, null)
                result.first().writerId shouldBe user1.id
            }
        }

        context("높은 순") {
            expect("평점 높은 순 정렬") {
                val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_DEFAULT_SIZE, PlaceReviewSortType.HIGHEST, null)
                result shouldBe listOf(placeReview1, placeReview2)
            }
        }

        context("낮은 순") {
            expect("평점 낮은 순 정렬") {
                val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_DEFAULT_SIZE, PlaceReviewSortType.LOWEST, null)
                result shouldBe listOf(placeReview2, placeReview1)
            }
        }

        context("최신순") {
            expect("최신순 정렬") {
                val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_OTHER_PLACE_ID, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, null)
                result shouldBe listOf(placeReview3)
            }
        }

        context("장소 리뷰 여부 확인(존재)") {
            expect("USER_ID와 PLACE_ID이 일치하는 장소 리뷰 여부 확인") {
                val result = placeReviewRepository.existsByUserIdAndPlaceId(user1.id, TEST_PLACE_ID)
                result shouldBe true
            }
        }

        context("장소 리뷰 여부 확인(존재하지 않음)") {
            expect("USER_ID와 PLACE_ID이 일치하는 장소 리뷰 여부 확인") {
                val result = placeReviewRepository.existsByUserIdAndPlaceId(TEST_OTHER_USER_ID, TEST_OTHER_PLACE_ID)
                result shouldBe false
            }
        }
    },
)
