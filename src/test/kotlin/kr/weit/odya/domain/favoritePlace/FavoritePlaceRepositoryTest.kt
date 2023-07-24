package kr.weit.odya.domain.favoritePlace

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_FAVORITE_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.createFavoritePlace
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class FavoritePlaceRepositoryTest(
    private val favoritePlaceRepository: FavoritePlaceRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        extensions(SpringTestExtension(SpringTestLifecycleMode.Root))
        val user: User = userRepository.save(createUser())
        favoritePlaceRepository.save(createFavoritePlace(user))

        context("관심 장소 조회") {

            expect("user와 placeId가 일치하는 관심 장소를 조회한다") {
                val result = favoritePlaceRepository.getByFavoritePlaceId(TEST_FAVORITE_PLACE_ID)
                result.id shouldBe TEST_FAVORITE_PLACE_ID
            }
        }

        context("관심 장소 여부 확인") {

            expect("USER_ID와 PLACE_ID이 일치하는 장소 리뷰 여부를 확인한다") {
                val result = favoritePlaceRepository.existsByUserIdAndPlaceId(user.id, TEST_PLACE_ID)
                result shouldBe true
            }
        }
    },
)
