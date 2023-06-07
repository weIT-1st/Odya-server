package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.security.FirebaseTokenParser
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PROVIDER
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.createLoginRequest
import kr.weit.odya.support.createRegisterRequest
import kr.weit.odya.support.createUser

class AuthenticationServiceTest : DescribeSpec({
    val userRepository = mockk<UserRepository>()
    val firebaseTokenParser = mockk<FirebaseTokenParser>()

    val authenticationService = AuthenticationService(userRepository, firebaseTokenParser)

    describe("loginProcess") {
        val loginRequest = createLoginRequest()
        context("유효한 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenParser.getUsername(loginRequest.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(TEST_USERNAME) } returns true
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { authenticationService.loginProcess(loginRequest) }
            }
        }

        context("유효하지 않은 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenParser.getUsername(loginRequest.idToken) } throws InvalidTokenException()
            it("[LoginFailedException] 예외가 발생한다") {
                shouldThrow<InvalidTokenException> { authenticationService.loginProcess(loginRequest) }
            }
        }

        context("가입하지 않은 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenParser.getUsername(loginRequest.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(TEST_USERNAME) } returns false
            it("[LoginFailedException] 예외가 발생한다") {
                shouldThrow<LoginFailedException> { authenticationService.loginProcess(loginRequest) }
            }
        }
    }

    describe("register") {
        val registerRequest = createRegisterRequest()
        context("유효한 회원가입 정보가 주어지는 경우") {
            every { firebaseTokenParser.getUsername(registerRequest.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(TEST_USERNAME) } returns false
            every { userRepository.existsByNickname(registerRequest.nickname) } returns false
            every { userRepository.save(any()) } returns createUser()
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { authenticationService.register(registerRequest, TEST_PROVIDER) }
            }
        }

        context("유효하지 않은 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenParser.getUsername(registerRequest.idToken) } throws InvalidTokenException()
            it("[LoginFailedException] 예외가 발생한다") {
                shouldThrow<InvalidTokenException> { authenticationService.register(registerRequest, TEST_PROVIDER) }
            }
        }

        context("이미 가입한 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenParser.getUsername(registerRequest.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(TEST_USERNAME) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.register(registerRequest, TEST_PROVIDER) }
            }
        }

        context("이미 가입한 닉네임이 주어지는 경우") {
            every { firebaseTokenParser.getUsername(registerRequest.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(TEST_USERNAME) } returns true
            every { userRepository.existsByNickname(TEST_NICKNAME) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.register(registerRequest, TEST_PROVIDER) }
            }
        }
    }

    describe("validateNickname") {
        context("중복이 없는 닉네임이 주어지는 경우") {
            every { userRepository.existsByNickname(TEST_NICKNAME) } returns false
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { authenticationService.validateNickname(TEST_NICKNAME) }
            }
        }

        context("중복이 있는 닉네임이 주어지는 경우") {
            every { userRepository.existsByNickname(TEST_NICKNAME) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.validateNickname(TEST_NICKNAME) }
            }
        }
    }
})
