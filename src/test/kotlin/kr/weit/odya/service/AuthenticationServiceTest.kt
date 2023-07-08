package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.client.kakao.KakaoClient
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.security.CreateFirebaseUserException
import kr.weit.odya.security.CreateTokenException
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.support.TEST_BEARER_OAUTH_ACCESS_TOKEN
import kr.weit.odya.support.TEST_CUSTOM_TOKEN
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_KAKAO_UID
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.createAppleRegisterRequest
import kr.weit.odya.support.createKakaoLoginRequest
import kr.weit.odya.support.createKakaoUserInfo
import kr.weit.odya.support.createUser

class AuthenticationServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val firebaseTokenHelper = mockk<FirebaseTokenHelper>()
        val kakaoClient = mockk<KakaoClient>()

        val authenticationService = AuthenticationService(userRepository, firebaseTokenHelper, kakaoClient)

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
                every { userRepository.existsByUsername(kakaoUserInfo.username) } returns true
                every { firebaseTokenHelper.createFirebaseCustomToken(kakaoUserInfo.username) } returns TEST_CUSTOM_TOKEN
                it("TokenResponse를 응답한다") {
                    authenticationService.kakaoLoginProcess(kakaoUserInfo).firebaseCustomToken shouldBe TEST_CUSTOM_TOKEN
                }
            }

            context("가입되어 있지 않은 USERNAME이 주어지는 경우") {
                every { userRepository.existsByUsername(kakaoUserInfo.username) } returns false
                it("[LoginFailedException] 예외가 발생한다") {
                    shouldThrow<LoginFailedException> { authenticationService.kakaoLoginProcess(kakaoUserInfo) }
                }
            }

            context("FIREBASE CUSTOM TOKEN 생성에 실패하는 경우") {
                every { userRepository.existsByUsername(kakaoUserInfo.username) } returns true
                every { firebaseTokenHelper.createFirebaseCustomToken(kakaoUserInfo.username) } throws CreateTokenException()
                it("[CreateTokenException] 예외가 발생한다") {
                    shouldThrow<CreateTokenException> { authenticationService.kakaoLoginProcess(kakaoUserInfo) }
                }
            }
        }

        describe("register") {
            val request = createAppleRegisterRequest()
            context("유효한 회원가입 정보가 주어지는 경우") {
                every { userRepository.existsByUsername(request.username) } returns false
                every { userRepository.existsByEmail(request.email!!) } returns false
                every { userRepository.existsByPhoneNumber(request.phoneNumber!!) } returns false
                every { userRepository.existsByNickname(request.nickname) } returns false
                every { userRepository.save(any()) } returns createUser()
                it("정상적으로 종료한다") {
                    shouldNotThrow<Exception> { authenticationService.register(request) }
                }
            }

            context("이미 가입한 USERNAME이 주어지는 경우") {
                every { userRepository.existsByUsername(request.username) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { authenticationService.register(request) }
                }
            }

            context("이미 가입한 이메일이 주어지는 경우") {
                every { userRepository.existsByUsername(request.username) } returns false
                every { userRepository.existsByEmail(request.email!!) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { authenticationService.register(request) }
                }
            }

            context("이미 가입한 전화번호가 주어지는 경우") {
                every { userRepository.existsByUsername(request.username) } returns false
                every { userRepository.existsByEmail(request.email!!) } returns false
                every { userRepository.existsByPhoneNumber(request.phoneNumber!!) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { authenticationService.register(request) }
                }
            }

            context("이미 가입한 닉네임이 주어지는 경우") {
                every { userRepository.existsByUsername(request.username) } returns false
                every { userRepository.existsByEmail(request.email!!) } returns false
                every { userRepository.existsByPhoneNumber(request.phoneNumber!!) } returns false
                every { userRepository.existsByNickname(request.nickname) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { authenticationService.register(request) }
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

        describe("createFirebaseUser") {
            context("유효한 USERNAME이 주어지는 경우") {
                every { firebaseTokenHelper.createFirebaseUser(TEST_USERNAME) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrow<Exception> { authenticationService.createFirebaseUser(TEST_USERNAME) }
                }
            }

            context("USER 생성 중 에러가 발생하는 경우") {
                every { firebaseTokenHelper.createFirebaseUser(TEST_USERNAME) } throws CreateFirebaseUserException()
                it("[CreateFirebaseUserException] 예외가 발생한다") {
                    shouldThrow<CreateFirebaseUserException> { authenticationService.createFirebaseUser(TEST_USERNAME) }
                }
            }
        }

        describe("getKakaoUserInfo") {
            context("유효한 요청이 주어지는 경우") {
                val request = createKakaoLoginRequest()
                val response = createKakaoUserInfo()
                every { kakaoClient.getKakaoUserInfo(TEST_BEARER_OAUTH_ACCESS_TOKEN) } returns response
                it("KakaoUserInfo를 반환한다") {
                    authenticationService.getKakaoUserInfo(request).username shouldBe TEST_KAKAO_UID
                }
            }

            context("WebClient 요청 중 예외가 발생하는 경우") {
                val request = createKakaoLoginRequest()
                every { kakaoClient.getKakaoUserInfo(TEST_BEARER_OAUTH_ACCESS_TOKEN) } throws RuntimeException()
                it("[RuntimeException] 예외가 발생한다") {
                    shouldThrow<RuntimeException> { authenticationService.getKakaoUserInfo(request) }
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

        describe("validateEmail") {
            context("중복이 없는 이메일이 주어지는 경우") {
                every { userRepository.existsByEmail(TEST_EMAIL) } returns false
                it("정상적으로 종료한다") {
                    shouldNotThrow<Exception> { authenticationService.validateEmail(TEST_EMAIL) }
                }
            }

            context("중복이 있는 이메일이 주어지는 경우") {
                every { userRepository.existsByEmail(TEST_EMAIL) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { authenticationService.validateEmail(TEST_EMAIL) }
                }
            }
        }

        describe("validatePhoneNumber") {
            context("중복이 없는 이메일이 주어지는 경우") {
                every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns false
                it("정상적으로 종료한다") {
                    shouldNotThrow<Exception> { authenticationService.validatePhoneNumber(TEST_PHONE_NUMBER) }
                }
            }

            context("중복이 있는 이메일이 주어지는 경우") {
                every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns true
                it("[ExistResourceException] 예외가 발생한다") {
                    shouldThrow<ExistResourceException> { authenticationService.validatePhoneNumber(TEST_PHONE_NUMBER) }
                }
            }
        }
    },
)
