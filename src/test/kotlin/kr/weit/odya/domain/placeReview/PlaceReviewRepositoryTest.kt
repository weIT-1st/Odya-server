package kr.weit.odya.domain.placeReview

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_PAGEABLE
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_SORT_TYPE
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class PlaceReviewRepositoryTest(
    private val placeReviewRepository: PlaceReviewRepository,
    private val userRepository: UserRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
    lateinit var user: User
    beforeEach {
        user = userRepository.save(createUser())
        placeReviewRepository.save(createPlaceReview(user))
    }

    context("장소 리뷰 조회") {
        expect("PLACE_REVIEW_ID와 일치하는 장소 리뷰를 조회한다") {
            val result = placeReviewRepository.getByPlaceReviewId(TEST_PLACE_REVIEW_ID)
            result.id shouldBe TEST_PLACE_REVIEW_ID
        }

        expect("PLACE_ID와 일치하는 장소 리뷰를 조회한다") {
            val result = placeReviewRepository.getPlaceReviewListByPlaceId(TEST_PLACE_ID, TEST_DEFAULT_PAGEABLE, TEST_PLACE_SORT_TYPE, null)
            result.first().placeId shouldBe TEST_PLACE_ID
        }

        expect("USER와 일치하는 장소 리뷰를 조회한다") {
            val result = placeReviewRepository.getPlaceReviewListByUser(user, TEST_DEFAULT_PAGEABLE, TEST_PLACE_SORT_TYPE, null)
            result.first().writerId shouldBe user.id
        }
    }

    context("장소 리뷰 여부 확인") {
        expect("USER_ID와 PLACE_ID이 일치하는 장소 리뷰 여부를 확인한다") {
            val result = placeReviewRepository.existsByUserIdAndPlaceId(user.id, TEST_PLACE_ID)
            result shouldBe true
        }
    }
},)
