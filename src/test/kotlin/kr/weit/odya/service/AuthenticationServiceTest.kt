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
import kr.weit.odya.security.CreateTokenException
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.service.dto.KakaoUserInfo
import kr.weit.odya.support.TEST_CUSTOM_TOKEN
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_KAKAO_UID
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.client.WebClientException
import kr.weit.odya.support.client.WebClientHelper
import kr.weit.odya.support.createAppleRegisterRequest
import kr.weit.odya.support.createKakaoLoginRequest
import kr.weit.odya.support.createKakaoUserInfo
import kr.weit.odya.support.createUser

class AuthenticationServiceTest : DescribeSpec({
    val userRepository = mockk<UserRepository>()
    val firebaseTokenHelper = mockk<FirebaseTokenHelper>()
    val webClientHelper = mockk<WebClientHelper>()

    val authenticationService = AuthenticationService(userRepository, firebaseTokenHelper, webClientHelper)

    describe("appleLoginProcess") {
        context("유효한 USERNAME이 주어지는 경우") {
            every { userRepository.existsByUsername(TEST_USERNAME) } returns true
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { authenticationService.appleLoginProcess(TEST_USERNAME) }
            }
        }

        context("가입되어 있지 않은 USERNAME이 주어지는 경우") {
            every { userRepository.existsByUsername(TEST_USERNAME) } returns false
            it("[LoginFailedException] 예외가 발생한다") {
                shouldThrow<LoginFailedException> { authenticationService.appleLoginProcess(TEST_USERNAME) }
            }
        }
    }

    describe("kakaoLoginProcess") {
        val kakaoUserInfo = createKakaoUserInfo()
        context("유효한 요청이 주어지는 경우") {
            every { userRepository.existsByUsername(kakaoUserInfo.uid) } returns true
            every { firebaseTokenHelper.createFirebaseCustomToken(kakaoUserInfo.uid) } returns TEST_CUSTOM_TOKEN
            it("TokenResponse를 응답한다") {
                authenticationService.kakaoLoginProcess(kakaoUserInfo).firebaseCustomToken shouldBe TEST_CUSTOM_TOKEN
            }
        }

        context("가입되어 있지 않은 UID가 주어지는 경우") {
            every { userRepository.existsByUsername(kakaoUserInfo.uid) } returns false
            it("[LoginFailedException] 예외가 발생한다") {
                shouldThrow<LoginFailedException> { authenticationService.kakaoLoginProcess(kakaoUserInfo) }
            }
        }

        context("FIREBASE CUSTOM TOKEN 생성에 실패하는 경우") {
            every { userRepository.existsByUsername(kakaoUserInfo.uid) } returns true
            every { firebaseTokenHelper.createFirebaseCustomToken(kakaoUserInfo.uid) } throws CreateTokenException()
            it("[CreateTokenException] 예외가 발생한다") {
                shouldThrow<CreateTokenException> { authenticationService.kakaoLoginProcess(kakaoUserInfo) }
            }
        }
    }

    describe("appleRegister") {
        val request = createAppleRegisterRequest()
        context("유효한 회원가입 정보가 주어지는 경우") {
            every { firebaseTokenHelper.getUid(request.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(TEST_USERNAME) } returns false
            every { userRepository.existsByNickname(request.nickname) } returns false
            every { userRepository.existsByEmail(TEST_EMAIL) } returns false
            every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns false
            every { userRepository.save(any()) } returns createUser()
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { authenticationService.appleRegister(request) }
            }
        }

        context("유효하지 않은 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenHelper.getUid(request.idToken) } throws InvalidTokenException()
            it("[InvalidTokenException] 예외가 발생한다") {
                shouldThrow<InvalidTokenException> { authenticationService.appleRegister(request) }
            }
        }

        context("이미 가입한 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenHelper.getUid(request.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(request.uid) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.appleRegister(request) }
            }
        }

        context("이미 가입한 이메일이 주어지는 경우") {
            every { firebaseTokenHelper.getUid(request.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(request.uid) } returns false
            every { userRepository.existsByEmail(request.email!!) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.appleRegister(request) }
            }
        }

        context("이미 가입한 전화번호가 주어지는 경우") {
            every { firebaseTokenHelper.getUid(request.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(request.uid) } returns false
            every { userRepository.existsByEmail(request.email!!) } returns false
            every { userRepository.existsByPhoneNumber(request.phoneNumber!!) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.appleRegister(request) }
            }
        }

        context("이미 가입한 닉네임이 주어지는 경우") {
            every { firebaseTokenHelper.getUid(request.idToken) } returns TEST_USERNAME
            every { userRepository.existsByUsername(request.uid) } returns false
            every { userRepository.existsByEmail(request.email!!) } returns false
            every { userRepository.existsByPhoneNumber(request.phoneNumber!!) } returns false
            every { userRepository.existsByNickname(request.nickname) } returns true
            it("[ExistResourceException] 예외가 발생한다") {
                shouldThrow<ExistResourceException> { authenticationService.appleRegister(request) }
            }
        }
    }

    describe("getUsernameByIdToken") {
        context("유효한 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenHelper.getUid(TEST_ID_TOKEN) } returns TEST_USERNAME
            it("정상적으로 종료한다") {
                shouldNotThrow<Exception> { authenticationService.getUsernameByIdToken(TEST_ID_TOKEN) }
            }
        }

        context("유효하지 않은 ID TOKEN이 주어지는 경우") {
            every { firebaseTokenHelper.getUid(TEST_ID_TOKEN) } throws InvalidTokenException()
            it("[InvalidTokenException] 예외가 발생한다") {
                shouldThrow<InvalidTokenException> { authenticationService.getUsernameByIdToken(TEST_ID_TOKEN) }
            }
        }
    }

    describe("getKakaoUserInfo") {
        context("유효한 요청이 주어지는 경우") {
            val request = createKakaoLoginRequest()
            val response = createKakaoUserInfo()
            every {
                webClientHelper.getWithHeader(HTTPS_KAPI_KAKAO_COM_V_2_USER_ME, KakaoUserInfo::class.java, any())
            } returns response
            it("KakaoUserInfo를 반환한다") {
                authenticationService.getKakaoUserInfo(request).uid shouldBe TEST_KAKAO_UID
            }
        }

        context("WebClient 요청 중 예외가 발생하는 경우") {
            val request = createKakaoLoginRequest()
            every {
                webClientHelper.getWithHeader(HTTPS_KAPI_KAKAO_COM_V_2_USER_ME, KakaoUserInfo::class.java, any())
            } throws WebClientException()
            it("[WebClientException] 예외가 발생한다") {
                shouldThrow<WebClientException> { authenticationService.getKakaoUserInfo(request) }
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
