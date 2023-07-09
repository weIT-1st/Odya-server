package kr.weit.odya.domain.placeReview

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_OTHER_PLACE_ID
import kr.weit.odya.support.TEST_OTHER_PLACE_REVIEW_ID_3
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.createOtherPlaceReview
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
            beforeEach {
                user1 = userRepository.save(createUser())
                user2 = userRepository.save(createOtherUser())
                placeReviewRepository.save(createPlaceReview(user1))
                placeReviewRepository.save(createOtherPlaceReview(user2))
                placeReviewRepository.save(createOtherPlaceReview(user1, TEST_OTHER_PLACE_REVIEW_ID_3, TEST_OTHER_PLACE_ID))
            }

            context("장소 리뷰 조회") {

                expect("PLACE_REVIEW_ID와 일치하는 장소 리뷰를 조회한다") {
                    val result = placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID)
                    result.id shouldBe TEST_PLACE_REVIEW_ID
                }

                expect("PLACE_ID와 일치하는 lastId 초과의 id인 장소 리뷰를 조회한다") {
                    val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID)
                    result.first().placeId shouldBe TEST_PLACE_ID
                    result.first().id shouldBeGreaterThan TEST_LAST_ID
                }

                expect("PLACE_ID와 일치하는 장소 리뷰를 size만큼 조회한다") {
                    val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, null)
                    result.size shouldBe TEST_SIZE + 1
                    result.first().placeId shouldBe TEST_PLACE_ID
                }

                expect("USER와 일치하는 lastId 초과의 id인 조회한다") {
                    val result = placeReviewRepository.getPlaceReviewListByUser(user1, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID)
                    result.first().writerId shouldBe user1.id
                    result.first().id shouldBeGreaterThan TEST_LAST_ID
                }

                expect("USER와 일치하는 장소 리뷰를 조회한다") {
                    val result = placeReviewRepository.getPlaceReviewListByUser(user1, TEST_DEFAULT_SIZE, TEST_PLACE_SORT_TYPE, null)
                    result.first().writerId shouldBe user1.id
                }
            }

            context("장소 리뷰 여부 확인") {

                expect("USER_ID와 PLACE_ID이 일치하는 장소 리뷰 여부를 확인한다") {
                    val result = placeReviewRepository.existsByUserIdAndPlaceId(user1.id, TEST_PLACE_ID)
                    result shouldBe true
                }
            }
        },
)
