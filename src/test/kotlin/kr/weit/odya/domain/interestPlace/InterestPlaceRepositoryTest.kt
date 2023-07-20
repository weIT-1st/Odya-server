package kr.weit.odya.domain.interestPlace

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_INTEREST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.createInterestPlace
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class InterestPlaceRepositoryTest(
    private val interestPlaceRepository: InterestPlaceRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
        lateinit var user: User
        beforeEach {
            user = userRepository.save(createUser())
            interestPlaceRepository.save(createInterestPlace(user))
        }

        context("관심 장소 조회") {

            expect("user와 placeId가 일치하는 관심 장소를 조회한다") {
                val result = interestPlaceRepository.getByInterestPlaceId(TEST_INTEREST_PLACE_ID)
                result.id shouldBe TEST_INTEREST_PLACE_ID
            }
        }

        context("관심 장소 여부 확인") {

            expect("USER_ID와 PLACE_ID이 일치하는 장소 리뷰 여부를 확인한다") {
                val result = interestPlaceRepository.existsByUserIdAndPlaceId(user.id, TEST_PLACE_ID)
                result shouldBe true
            }
        }
    },
)
