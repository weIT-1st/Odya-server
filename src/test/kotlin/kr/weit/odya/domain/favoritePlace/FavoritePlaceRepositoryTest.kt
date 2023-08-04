package kr.weit.odya.domain.favoritePlace

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FAVORITE_PLACE_ID
import kr.weit.odya.support.TEST_FAVORITE_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFavoritePlace
import kr.weit.odya.support.createOtherFavoritePlace
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
        lateinit var favoritePlace: FavoritePlace
        lateinit var otherFavoritePlace: FavoritePlace
        beforeEach {
            favoritePlace = favoritePlaceRepository.save(createFavoritePlace(user))
            otherFavoritePlace = favoritePlaceRepository.save(createOtherFavoritePlace(user))
        }

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

        context("관심 장소 수 확인") {
            expect("userId가 일치하는 관심 장소를 조회한다") {
                val result = favoritePlaceRepository.countByUserId(user.id)
                result shouldBe 2
            }
        }

        context("관심 장소 목록") {
            expect("userId가 일치하는 관심 장소 목록을 조회한다") {
                val result = favoritePlaceRepository.getByFavoritePlaceList(user, TEST_DEFAULT_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, otherFavoritePlace.id)
                result.size shouldBe 1
                result.first() shouldBe favoritePlace
            }
        }

        context("관심 장소 목록 (lastId가 null인 경우)") {
            expect("userId가 일치하는 관심 장소 목록을 조회한다") {
                val result = favoritePlaceRepository.getByFavoritePlaceList(user, TEST_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, null)
                result.size shouldBe TEST_SIZE + 1
                result.first() shouldBe otherFavoritePlace
            }
        }

        context("관심 장소 삭제") {
            expect("user와 placeId가 일치하는 관심 장소를 삭제한다") {
                favoritePlaceRepository.delete(favoritePlace)
                favoritePlaceRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) shouldBe false
            }
        }
    },
)