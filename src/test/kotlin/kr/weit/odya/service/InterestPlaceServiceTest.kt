package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kr.weit.odya.domain.interestPlace.InterestPlaceRepository
import kr.weit.odya.domain.interestPlace.getByInterestPlaceId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_INTEREST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createInterestPlace
import kr.weit.odya.support.createInterestPlaceRequest
import kr.weit.odya.support.createUser

class InterestPlaceServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val interestPlaceRepository = mockk<InterestPlaceRepository>()
        val sut = InterestPlaceService(interestPlaceRepository, userRepository)
        val user = createUser()

        describe("createInterestPlace 메소드") {
            context("유효한 데이터가 전달되면") {
                every { interestPlaceRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { interestPlaceRepository.save(any()) } returns createInterestPlace(user)
                it("관심 장소로 등록한다") {
                    shouldNotThrowAny { sut.createInterestPlace(TEST_USER_ID, createInterestPlaceRequest()) }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.createInterestPlace(TEST_USER_ID, createInterestPlaceRequest()) }
                }
            }

            context("이미 관심 장소인 경우") {
                every { interestPlaceRepository.existsByUserIdAndPlaceId(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { sut.createInterestPlace(TEST_USER_ID, createInterestPlaceRequest()) }
                }
            }
        }

        describe("deleteInterestPlace 메소드") {
            context("유효한 데이터가 전달되면") {
                every { interestPlaceRepository.getByInterestPlaceId(TEST_INTEREST_PLACE_ID) } returns createInterestPlace(user)
                every { interestPlaceRepository.delete(any()) } just Runs
                it("관심 장소를 해제한다.") {
                    shouldNotThrowAny { sut.deleteInterestPlace(TEST_USER_ID, TEST_INTEREST_PLACE_ID) }
                }
            }

            context("등록된 관심 장소가 아닌 경우") {
                every { interestPlaceRepository.getByInterestPlaceId(TEST_INTEREST_PLACE_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.deleteInterestPlace(TEST_USER_ID, TEST_INTEREST_PLACE_ID) }
                }
            }
        }
    },
)
