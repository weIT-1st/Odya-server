package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kr.weit.odya.domain.favoritePlace.FavoritePlaceRepository
import kr.weit.odya.domain.favoritePlace.getByFavoritePlaceId
import kr.weit.odya.domain.favoritePlace.getByFavoritePlaceList
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FAVORITE_PLACE_COUNT
import kr.weit.odya.support.TEST_FAVORITE_PLACE_ID
import kr.weit.odya.support.TEST_FAVORITE_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFavoritePlace
import kr.weit.odya.support.createFavoritePlaceList
import kr.weit.odya.support.createFavoritePlaceRequest
import kr.weit.odya.support.createUser

class FavoritePlaceServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val favoritePlaceRepository = mockk<FavoritePlaceRepository>()
        val sut = FavoritePlaceService(favoritePlaceRepository, userRepository)
        val user = createUser()

        describe("createFavoritePlace 메소드") {
            context("유효한 USERID와 FavoritePlaceRequest가 전달되면") {
                every { favoritePlaceRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { favoritePlaceRepository.save(any()) } returns createFavoritePlace(user)
                it("관심 장소로 등록한다") {
                    shouldNotThrowAny { sut.createFavoritePlace(TEST_USER_ID, createFavoritePlaceRequest()) }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.createFavoritePlace(TEST_USER_ID, createFavoritePlaceRequest()) }
                }
            }

            context("이미 관심 장소인 경우") {
                every { favoritePlaceRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { sut.createFavoritePlace(TEST_USER_ID, createFavoritePlaceRequest()) }
                }
            }
        }

        describe("deleteFavoritePlace 메소드") {
            context("유효한 USERID와 관심장소ID가 전달되면") {
                every { favoritePlaceRepository.getByFavoritePlaceId(TEST_FAVORITE_PLACE_ID) } returns createFavoritePlace(user)
                every { favoritePlaceRepository.delete(any()) } just Runs
                it("관심 장소를 해제한다.") {
                    shouldNotThrowAny { sut.deleteFavoritePlace(TEST_USER_ID, TEST_FAVORITE_PLACE_ID) }
                }
            }

            context("등록된 관심 장소가 아닌 경우") {
                every { favoritePlaceRepository.getByFavoritePlaceId(TEST_FAVORITE_PLACE_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.deleteFavoritePlace(TEST_USER_ID, TEST_FAVORITE_PLACE_ID) }
                }
            }
        }

        describe("getFavoritePlace 메소드") {
            context("유효한 USERID와 장소ID가 전달되면") {
                every { favoritePlaceRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("관심 장소 여부를 확인한다.") {
                    shouldNotThrowAny { sut.getFavoritePlace(TEST_USER_ID, TEST_PLACE_ID) }
                }
            }
        }

        describe("getFavoritePlaceCount 메소드") {
            context("유효한 USERID가 전달되면") {
                every { favoritePlaceRepository.countByUserId(TEST_USER_ID) } returns TEST_FAVORITE_PLACE_COUNT
                it("관심 장소 수를 출력한다.") {
                    shouldNotThrowAny { sut.getFavoritePlaceCount(TEST_USER_ID) }
                }
            }
        }

        describe("getFavoritePlaceList 메소드") {
            context("유효한 USERID와 size,sortType,lastId가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { favoritePlaceRepository.getByFavoritePlaceList(user, TEST_DEFAULT_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, TEST_LAST_ID) } returns createFavoritePlaceList()
                it("관심 장소 수를 출력한다.") {
                    shouldNotThrowAny { sut.getFavoritePlaceList(TEST_USER_ID, TEST_DEFAULT_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, TEST_LAST_ID) }
                }
            }

            context("가입되지 않은 USERID가 전달되면") {
                every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                it("NoSuchElementException 예외가 발생한다.") {
                    shouldThrow<NoSuchElementException> { sut.getFavoritePlaceList(TEST_NOT_EXIST_USER_ID, TEST_DEFAULT_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, TEST_LAST_ID) }
                }
            }
        }
    },
)
