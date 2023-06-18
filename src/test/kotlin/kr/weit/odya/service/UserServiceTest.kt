package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.security.FirebaseTokenParser
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createInformationRequest
import kr.weit.odya.support.createUser
import kr.weit.odya.support.createUserResponse

class UserServiceTest : DescribeSpec({
    val userRepository = mockk<UserRepository>()
    val firebaseTokenParser = mockk<FirebaseTokenParser>()

    val userService = UserService(userRepository, firebaseTokenParser)

    describe("getInformation") {
        context("가입되어 있는 USER ID가 주어지는 경우") {
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            it("UserResponse를 반환한다") {
                val userResponse = userService.getInformation(TEST_USER_ID)
                userResponse shouldBe createUserResponse()
            }
        }

        context("가입되어 있지 않은 USER ID가 주어지는 경우") {
            every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
            it("[NoSuchElementException] 예외가 발생한다") {
                shouldThrow<NoSuchElementException> { userService.getInformation(TEST_USER_ID) }
            }
        }
    }

    describe("getEmailByIdToken") {
        context("유효하고 인증된 이메일이 있는 토큰이 주어지는 경우") {
            every { firebaseTokenParser.getEmail(TEST_ID_TOKEN) } returns TEST_EMAIL
            it("이메일을 반환한다") {
                val response = userService.getEmailByIdToken(TEST_ID_TOKEN)
                response shouldBe TEST_EMAIL
            }
        }

        context("유효하지만 인증된 이메일이 없는 토큰이 주어지는 경우") {
            every { firebaseTokenParser.getEmail(TEST_ID_TOKEN) } throws NoSuchElementException()
            it("[NoSuchElementException] 반환한다") {
                shouldThrow<NoSuchElementException> { userService.getEmailByIdToken(TEST_ID_TOKEN) }
            }
        }

        context("유효하지 않은 토큰이 주어지는 경우") {
            every { firebaseTokenParser.getEmail(TEST_ID_TOKEN) } throws InvalidTokenException()
            it("[InvalidTokenException] 반환한다") {
                shouldThrow<InvalidTokenException> { userService.getEmailByIdToken(TEST_ID_TOKEN) }
            }
        }
    }

    describe("getPhoneNumberByIdToken") {
        context("유효하고 인증된 전화번호가 있는 토큰이 주어지는 경우") {
            every { firebaseTokenParser.getPhoneNumber(TEST_ID_TOKEN) } returns TEST_PHONE_NUMBER
            it("전화번호를 반환한다") {
                val response = userService.getPhoneNumberByIdToken(TEST_ID_TOKEN)
                response shouldBe TEST_PHONE_NUMBER
            }
        }

        context("유효하지만 인증된 전화번호가 없는 토큰이 주어지는 경우") {
            every { firebaseTokenParser.getPhoneNumber(TEST_ID_TOKEN) } throws NoSuchElementException()
            it("[NoSuchElementException] 반환한다") {
                shouldThrow<NoSuchElementException> { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) }
            }
        }

        context("유효하지 않은 토큰이 주어지는 경우") {
            every { firebaseTokenParser.getPhoneNumber(TEST_ID_TOKEN) } throws InvalidTokenException()
            it("[InvalidTokenException] 반환한다") {
                shouldThrow<InvalidTokenException> { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) }
            }
        }
    }

    describe("updateEmail") {
        context("가입되어 있는 USER ID와 유효한 이메일이 주어지는 경우") {
            every { userRepository.existsByEmail(TEST_EMAIL) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) }
            }
        }

        context("이미 존재하는 이메일인 경우") {
            every { userRepository.existsByEmail(TEST_EMAIL) } returns true
            it("[ExistResourceException] 반환한다") {
                shouldThrow<ExistResourceException> { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) }
            }
        }

        context("유효한 이메일이지만, 가입되어 있지 않은 USER ID인 경우") {
            every { userRepository.existsByEmail(TEST_EMAIL) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
            it("[NoSuchElementException] 반환한다") {
                shouldThrow<NoSuchElementException> { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) }
            }
        }
    }

    describe("updatePhoneNumber") {
        context("가입되어 있는 USER ID와 유효한 전화번호가 주어지는 경우") {
            every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER) }
            }
        }

        context("이미 존재하는 전화번호인 경우") {
            every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns true
            it("[ExistResourceException] 반환한다") {
                shouldThrow<ExistResourceException> { userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER) }
            }
        }

        context("유효한 전화번호가 주어지지만, 가입되어 있지 않은 USER ID인 경우") {
            every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
            it("[NoSuchElementException] 반환한다") {
                shouldThrow<NoSuchElementException> { userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER) }
            }
        }
    }

    describe("updateInformation") {
        val informationRequest = createInformationRequest()
        context("가입되어 있는 USER ID와 유효한 정보 요청이 주어지는 경우") {
            every { userRepository.existsByNickname(TEST_NICKNAME) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { userService.updateInformation(TEST_USER_ID, informationRequest) }
            }
        }

        context("이미 존재하는 닉네임이 주어지는 경우") {
            every { userRepository.existsByNickname(TEST_NICKNAME) } returns true
            it("[ExistResourceException] 반환한다") {
                shouldThrow<ExistResourceException> { userService.updateInformation(TEST_USER_ID, informationRequest) }
            }
        }

        context("유효한 정보 요청이지만, 가입되어 있지 않은 USER ID인 경우") {
            every { userRepository.existsByNickname(TEST_NICKNAME) } returns false
            every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
            it("[NoSuchElementException] 반환한다") {
                shouldThrow<NoSuchElementException> { userService.updateInformation(TEST_USER_ID, informationRequest) }
            }
        }
    }
})
