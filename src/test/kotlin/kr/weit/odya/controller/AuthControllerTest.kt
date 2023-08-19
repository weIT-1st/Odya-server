package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.client.KakaoClientException
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.security.CreateFirebaseCustomTokenException
import kr.weit.odya.security.CreateFirebaseUserException
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.service.AuthenticationService
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.TermsService
import kr.weit.odya.service.UnRegisteredUserException
import kr.weit.odya.support.ALREADY_REGISTER_USER_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_NICKNAME_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.NOT_FOUND_REQUIRED_TERMS_ERROR_MESSAGE
import kr.weit.odya.support.NOT_FOUND_TERMS_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_INVALID_EMAIL
import kr.weit.odya.support.TEST_INVALID_PHONE_NUMBER
import kr.weit.odya.support.TEST_INVALID_TERMS_ID
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_NOT_EXIST_TERMS_ID
import kr.weit.odya.support.TEST_OTHER_TERMS_ID
import kr.weit.odya.support.TEST_OTHER_TERMS_ID_2
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_TERMS_ID
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.createAppleLoginRequest
import kr.weit.odya.support.createAppleRegisterRequest
import kr.weit.odya.support.createKakaoLoginRequest
import kr.weit.odya.support.createKakaoRegisterErrorResponse
import kr.weit.odya.support.createKakaoRegisterRequest
import kr.weit.odya.support.createKakaoUserInfo
import kr.weit.odya.support.createTermsContentResponse
import kr.weit.odya.support.createTermsListResponse
import kr.weit.odya.support.createTokenResponse
import kr.weit.odya.support.createUser
import kr.weit.odya.support.exception.ErrorCode
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.generateRestDocMockMvc
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.example
import kr.weit.odya.support.test.parameterDescription
import kr.weit.odya.support.test.pathDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(AuthController::class)
class AuthControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val authenticationService: AuthenticationService,
    @MockkBean private val termsService: TermsService,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/auth/login/appple") {
            val targetUri = "/api/v1/auth/login/apple"
            context("유효하고 가입된 토큰이 전달되면") {
                val request = createAppleLoginRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.appleLoginProcess(TEST_USERNAME) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "apple-login-success",
                            requestBody("idToken" type JsonFieldType.STRING description "유효하고 회원가입된 ID TOKEN" example request.idToken),
                        )
                    }
                }
            }

            context("유효하지만 서버에 가입되지 않은 회원의 토큰이 전달되면") {
                val request = createAppleLoginRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.appleLoginProcess(TEST_USERNAME) } throws UnRegisteredUserException(
                    NOT_EXIST_USER_ERROR_MESSAGE,
                )
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "apple-login-fail-not-registered-token",
                            requestBody("idToken" type JsonFieldType.STRING description "유효하지만 회원가입되지 않은 ID TOKEN" example request.idToken),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = createAppleLoginRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } throws InvalidTokenException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "apple-login-fail-invalid-token",
                            requestBody("idToken" type JsonFieldType.STRING description "유효하지 않은 ID TOKEN" example request.idToken),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/auth/login/kakao") {
            val targetUri = "/api/v1/auth/login/kakao"
            context("유효하고 가입된 토큰이 전달되면") {
                val request = createKakaoLoginRequest()
                val kakaoUserInfo = createKakaoUserInfo()
                val response = createTokenResponse()
                every { authenticationService.getKakaoUserInfo(request) } returns kakaoUserInfo
                every { authenticationService.kakaoLoginProcess(kakaoUserInfo) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "kakao-login-success",
                            requestBody("accessToken" type JsonFieldType.STRING description "유효하고 회원가입된 OAUTH ACCESS TOKEN" example request.accessToken),
                            responseBody("firebaseCustomToken" type JsonFieldType.STRING description "FIREBASE CUSTOM TOKEN" example response.firebaseCustomToken),
                        )
                    }
                }
            }

            context("유효하지만 가입되지 않은 토큰이 전달되면") {
                val request = createKakaoLoginRequest()
                val kakaoUserInfo = createKakaoUserInfo()
                val response = createKakaoRegisterErrorResponse(kakaoUserInfo)
                every { authenticationService.getKakaoUserInfo(request) } returns kakaoUserInfo
                every { authenticationService.kakaoLoginProcess(kakaoUserInfo) } throws UnRegisteredUserException(
                    NOT_EXIST_USER_ERROR_MESSAGE,
                )
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "kakao-login-fail-not-registered-token",
                            requestBody("accessToken" type JsonFieldType.STRING description "유효하지만 회원가입되지 않은 OAUTH ACCESS TOKEN" example request.accessToken),
                            responseBody(
                                "code" type JsonFieldType.NUMBER description "에러 코드" example ErrorCode.UNREGISTERED_USER.code,
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example NOT_EXIST_USER_ERROR_MESSAGE,
                                "data.username" type JsonFieldType.STRING description "회원 고유 아이디" example response.data.username,
                                "data.email" type JsonFieldType.STRING description "회원 이메일" example response.data.email isOptional true,
                                "data.phoneNumber" type JsonFieldType.STRING description "회원 전화번호" example response.data.phoneNumber isOptional true,
                                "data.nickname" type JsonFieldType.STRING description "회원 닉네임" example response.data.nickname,
                                "data.gender" type JsonFieldType.STRING description "회원 성별" example response.data.gender isOptional true,
                            ),
                        )
                    }
                }
            }

            context("카카오 회원 정보 요청 중, 예외가 발생하면") {
                val request = createKakaoLoginRequest()
                every { authenticationService.getKakaoUserInfo(request) } throws KakaoClientException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("500 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "kakao-login-fail-webclient-error",
                            requestBody("accessToken" type JsonFieldType.STRING description "유효한 OAUTH ACCESS TOKEN" example request.accessToken),
                        )
                    }
                }
            }

            context("FIREBASE 커스텀 토큰 생성에 실패하면") {
                val request = createKakaoLoginRequest()
                val kakaoUserInfo = createKakaoUserInfo()
                every { authenticationService.getKakaoUserInfo(request) } returns kakaoUserInfo
                every { authenticationService.kakaoLoginProcess(kakaoUserInfo) } throws CreateFirebaseCustomTokenException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("500 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "kakao-login-fail-create-token-error",
                            requestBody("accessToken" type JsonFieldType.STRING description "유효한 OAUTH ACCESS TOKEN" example request.accessToken),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/auth/register/apple") {
            val targetUri = "/api/v1/auth/register/apple"
            val user = createUser()
            context("유효한 회원가입 정보가 전달되면") {
                val request = createAppleRegisterRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.register(request) } returns user
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { termsService.saveAllAgreedTerms(user, request.termsIdList) } just Runs
                it("201 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "apple-register-success",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("FIREBASE에 등록되지 않은 ID TOKEN이 주어지면") {
                val request = createAppleRegisterRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } throws InvalidTokenException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-not-register-token",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "FIREBASE에 등록되지 않은 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 필수 약관이 모두 포함되지 않은 리스트가 전달되면") {
                val request = createAppleRegisterRequest().copy(termsIdList = setOf(TEST_OTHER_TERMS_ID, TEST_OTHER_TERMS_ID_2))
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { termsService.checkRequiredTerms(request.termsIdList) } throws NoSuchElementException(
                    NOT_FOUND_REQUIRED_TERMS_ERROR_MESSAGE,
                )
                it("404 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-not-found-required-terms",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "필수 약관이 모두 포함되지 않은 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 사용자면") {
                val request = createAppleRegisterRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_USER_ERROR_MESSAGE,
                )
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-exist-user",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "이미 가입된 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 이메일이면") {
                val request = createAppleRegisterRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_EMAIL_ERROR_MESSAGE,
                )
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-exist-email",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "이미 존재하는 사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 전화번호이면") {
                val request = createAppleRegisterRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_PHONE_NUMBER_ERROR_MESSAGE,
                )
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-exist-phone-number",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "이미 존재하는 사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 닉네임이면") {
                val request = createAppleRegisterRequest()
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_NICKNAME_ERROR_MESSAGE,
                )
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-exist-nickname",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "이미 존재하는 사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 존재하지 않는 약관 ID가 포함되어 있으면") {
                val request = createAppleRegisterRequest().copy(termsIdList = setOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID_2, TEST_NOT_EXIST_TERMS_ID))
                every { authenticationService.getUsernameByIdToken(request.idToken) } returns TEST_USERNAME
                every { authenticationService.register(request) } returns user
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { termsService.saveAllAgreedTerms(user, request.termsIdList) } throws NoSuchElementException(
                    NOT_FOUND_TERMS_ERROR_MESSAGE,
                )
                it("404 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-not-found-terms",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "존재하지 않는 약관 ID가 포함 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 유효하지 않은 형식의 이메일이면") {
                val request = createAppleRegisterRequest().copy(email = TEST_INVALID_EMAIL)
                it("400 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-invalid-email",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "올바르지 않은 형식의 사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 유효하지 않은 형식의 전화번호이면") {
                val request = createAppleRegisterRequest().copy(phoneNumber = TEST_INVALID_PHONE_NUMBER)
                it("400 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-invalid-phone-number",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "올바르지 않은 형식의 사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 빈 약관 ID 리스트가 전달되면") {
                val request = createAppleRegisterRequest().copy(termsIdList = emptySet())
                it("400 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "apple-register-fail-empty-terms-id-list",
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "올바르지 않은 형식의 사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "빈 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/auth/register/kakao") {
            val targetUri = "/api/v1/auth/register/kakao"
            val user = createUser()
            context("유효한 회원가입 정보가 전달되면") {
                val request = createKakaoRegisterRequest()
                every { authenticationService.register(request) } returns user
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { termsService.saveAllAgreedTerms(user, request.termsIdList) } just Runs
                it("201 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "kakao-register-success",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("FIREBASE에 이미 존재하는 USERNAME이 전달되면") {
                val request = createKakaoRegisterRequest()
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { authenticationService.register(request) } throws CreateFirebaseUserException(
                    ALREADY_REGISTER_USER_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-exist-username-in-firebase",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 필수 약관이 모두 포함되지 않은 리스트가 전달되면") {
                val request = createKakaoRegisterRequest().copy(termsIdList = setOf(TEST_OTHER_TERMS_ID, TEST_OTHER_TERMS_ID_2))
                every { authenticationService.register(request) } returns user
                every { termsService.checkRequiredTerms(request.termsIdList) } throws NoSuchElementException(NOT_FOUND_REQUIRED_TERMS_ERROR_MESSAGE)
                it("404 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-not-found-required-terms",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "필수 약관이 모두 포함되지 않은 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 사용자면") {
                val request = createKakaoRegisterRequest()
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_USER_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-exist-user",
                            requestBody(
                                "username" type JsonFieldType.STRING description "이미 가입된 USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 이메일이면") {
                val request = createKakaoRegisterRequest()
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_EMAIL_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-exist-email",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "이미 존재하는 사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 전화번호이면") {
                val request = createKakaoRegisterRequest()
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_PHONE_NUMBER_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-exist-phone-number",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "이미 존재하는 사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 존재하는 닉네임이면") {
                val request = createKakaoRegisterRequest()
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { authenticationService.register(request) } throws ExistResourceException(
                    EXIST_NICKNAME_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-exist-nickname",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "이미 존재하는 사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 존재하지 않는 약관 ID가 리스트 포함되어 있으면") {
                val request = createKakaoRegisterRequest().copy(termsIdList = setOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID_2, TEST_NOT_EXIST_TERMS_ID))
                every { authenticationService.register(request) } returns user
                every { termsService.checkRequiredTerms(request.termsIdList) } just Runs
                every { termsService.saveAllAgreedTerms(user, request.termsIdList) } throws NoSuchElementException(NOT_FOUND_TERMS_ERROR_MESSAGE)
                it("404 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-not-found-terms",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "존재하지 않는 약관 ID가 포함된 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 유효하지 않은 형식의 이메일이면") {
                val request = createKakaoRegisterRequest().copy(email = TEST_INVALID_EMAIL)
                it("400 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-invalid-email",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "올바르지 않은 형식의 사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 유효하지 않은 형식의 전화번호이면") {
                val request = createKakaoRegisterRequest().copy(phoneNumber = TEST_INVALID_PHONE_NUMBER)
                it("400 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-invalid-phone-number",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "올바르지 않은 형식의 사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "동의한 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 빈 약관 ID 리스트이면") {
                val request = createKakaoRegisterRequest().copy(termsIdList = emptySet())
                it("400 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "kakao-register-fail-empty-terms-id-list",
                            requestBody(
                                "username" type JsonFieldType.STRING description "USERNAME" example request.username,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday,
                                "termsIdList" type JsonFieldType.ARRAY description "빈 약관 ID 리스트" example request.termsIdList,
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/auth/validate/nickname") {
            val targetUri = "/api/v1/auth/validate/nickname"
            context("중복되지 않는 닉네임이면") {
                every { authenticationService.validateNickname(TEST_NICKNAME) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        param("value", TEST_NICKNAME)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "validate-nickname-success",
                            queryParameters(
                                "value" parameterDescription "중복되지 않은 사용자 닉네임" example TEST_NICKNAME,
                            ),
                        )
                    }
                }
            }

            context("중복인 닉네임이면") {
                every { authenticationService.validateNickname(TEST_NICKNAME) } throws ExistResourceException(
                    EXIST_NICKNAME_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        param("value", TEST_NICKNAME)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "validate-nickname-fail-exist-nickname",
                            queryParameters(
                                "value" parameterDescription "중복된 사용자 닉네임" example TEST_NICKNAME,
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/auth/validate/email") {
            val targetUri = "/api/v1/auth/validate/email"
            context("중복되지 않는 이메일이면") {
                every { authenticationService.validateEmail(TEST_EMAIL) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        param("value", TEST_EMAIL)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "validate-email-success",
                            queryParameters(
                                "value" parameterDescription "중복되지 않은 사용자 이메일" example TEST_EMAIL,
                            ),
                        )
                    }
                }
            }

            context("중복인 이메일이면") {
                every { authenticationService.validateEmail(TEST_EMAIL) } throws ExistResourceException(
                    EXIST_EMAIL_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        param("value", TEST_EMAIL)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "validate-email-fail-exist-nickname",
                            queryParameters(
                                "value" parameterDescription "중복된 사용자 이메일" example TEST_EMAIL,
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/auth/validate/phone-number") {
            val targetUri = "/api/v1/auth/validate/phone-number"
            context("중복되지 않는 전화번호이면") {
                every { authenticationService.validatePhoneNumber(TEST_PHONE_NUMBER) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        param("value", TEST_PHONE_NUMBER)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "validate-phone-number-success",
                            queryParameters(
                                "value" parameterDescription "중복되지 않은 사용자 전화번호" example TEST_PHONE_NUMBER,
                            ),
                        )
                    }
                }
            }

            context("중복인 전화번호면") {
                every { authenticationService.validatePhoneNumber(TEST_PHONE_NUMBER) } throws ExistResourceException(
                    EXIST_PHONE_NUMBER_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        param("value", TEST_PHONE_NUMBER)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "validate-phone-number-fail-exist-nickname",
                            queryParameters(
                                "value" parameterDescription "중복된 사용자 전화번호" example TEST_PHONE_NUMBER,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/auth/terms") {
            val targetUri = "/api/v1/auth/terms"
            context("유효한 요청이 전달되면") {
                val response = createTermsListResponse()
                every { termsService.getTermsList() } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.get(targetUri)
                        .andExpect {
                            status { isOk() }
                        }.andDo {
                            createDocument(
                                "terms-list-success",
                                responseBody(
                                    "[]" type JsonFieldType.ARRAY description "약관 목록" example response,
                                    "[].id" type JsonFieldType.NUMBER description "약관 ID" example response[0].id,
                                    "[].title" type JsonFieldType.STRING description "약관 제목" example response[0].title,
                                    "[].required" type JsonFieldType.NUMBER description "필수 여부" example response[0].required,
                                ),
                            )
                        }
                }
            }
        }

        describe("GET /api/v1/auth/terms/{id}") {
            val targetUri = "/api/v1/auth/terms/{id}"
            context("유효한 약관ID가 전달되면") {
                val response = createTermsContentResponse()
                every { termsService.getTermsContent(TEST_TERMS_ID) } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_TERMS_ID),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "terms-get-success",
                                pathParameters(
                                    "id" pathDescription "약관 ID" example TEST_TERMS_ID,
                                ),
                                responseBody(
                                    "id" type JsonFieldType.NUMBER description "약관 ID" example response.id,
                                    "content" type JsonFieldType.STRING description "약관 내용" example response.content,
                                ),
                            ),
                        )
                }
            }

            context("양수가 아닌 약관 ID가 전달되면") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_TERMS_ID),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "terms-get-fail-invalid-id",
                                pathParameters(
                                    "id" pathDescription "양수가 아닌 약관 ID" example TEST_INVALID_TERMS_ID,
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 약관ID가 전달되면") {
                every { termsService.getTermsContent(TEST_NOT_EXIST_TERMS_ID) } throws NoSuchElementException(NOT_FOUND_TERMS_ERROR_MESSAGE)
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_NOT_EXIST_TERMS_ID),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "terms-get-fail-not-exist-id",
                                pathParameters(
                                    "id" pathDescription "존재하지 않는 약관 ID" example TEST_NOT_EXIST_TERMS_ID,
                                ),
                            ),
                        )
                }
            }
        }

        afterEach {
            restDocumentation.afterTest()
        }
    },
)
