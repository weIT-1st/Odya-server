package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.service.AuthenticationService
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.LoginFailedException
import kr.weit.odya.support.EXIST_NICKNAME_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PROVIDER
import kr.weit.odya.support.TOKEN_ERROR_MESSAGE
import kr.weit.odya.support.createInvalidEmailRegisterRequest
import kr.weit.odya.support.createInvalidPhoneNumberRegisterRequest
import kr.weit.odya.support.createLoginRequest
import kr.weit.odya.support.createRegisterRequest
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
import org.springframework.http.MediaType
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
    @MockkBean private val authenticationService: AuthenticationService
) : DescribeSpec({
    val restDocumentation = ManualRestDocumentation()
    val restDocMockMvc = generateRestDocMockMvc(context, restDocumentation)

    beforeEach {
        restDocumentation.beforeTest(javaClass, it.name.testName)
    }

    describe("POST /api/v1/auth/login") {
        val targetUri = "/api/v1/auth/login"
        context("유효하고 가입된 토큰이 전달되면") {
            val request = createLoginRequest()
            every { authenticationService.loginProcess(request) } just runs
            it("204 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    jsonContent(request)
                }.andExpect {
                    status { isNoContent() }
                }.andDo {
                    createDocument(
                        "login-success",
                        requestBody("idToken" type JsonFieldType.STRING description "유효하고 회원가입된 ID TOKEN" example request.idToken)
                    )
                }
            }
        }

        context("유효하지만 가입되지 않은 토큰이 전달되면") {
            val request = createLoginRequest()
            every { authenticationService.loginProcess(request) } throws LoginFailedException(
                NOT_EXIST_USER_ERROR_MESSAGE
            )
            it("401 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    jsonContent(request)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "login-fail-not-registered-token",
                        requestBody("idToken" type JsonFieldType.STRING description "유효하지만 회원가입되지 않은 ID TOKEN" example request.idToken),
                        responseBody("errorMessage" type JsonFieldType.STRING description "에러 메시지")
                    )
                }
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            val request = createLoginRequest()
            every { authenticationService.loginProcess(request) } throws InvalidTokenException(TOKEN_ERROR_MESSAGE)
            it("401 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    jsonContent(request)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "login-fail-invalid-token",
                        requestBody("idToken" type JsonFieldType.STRING description "유효하지 않은 ID TOKEN" example request.idToken),
                        responseBody("errorMessage" type JsonFieldType.STRING description "에러 메시지")
                    )
                }
            }
        }
    }

    describe("POST /api/v1/auth/register/{provider}") {
        val targetUri = "/api/v1/auth/register/{provider}"
        context("유효한 회원가입 정보가 전달되면") {
            val request = createRegisterRequest()
            every { authenticationService.register(request, TEST_PROVIDER) } just runs
            it("201 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .post(targetUri, TEST_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent(request))
                )
                    .andExpect(status().isCreated)
                    .andDo(
                        createPathDocument(
                            "register-success",
                            pathParameters(
                                "provider" pathDescription "소셜 타입" example SocialType.values()
                                    .joinToString { value -> value.name.lowercase() }
                            ),
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday.toString()
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이지만, 이미 존재하는 사용자면") {
            val request = createRegisterRequest()
            every { authenticationService.register(request, TEST_PROVIDER) } throws ExistResourceException(
                EXIST_USER_ERROR_MESSAGE
            )
            it("409 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .post(targetUri, TEST_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent(request))
                )
                    .andExpect(status().isConflict)
                    .andDo(
                        createPathDocument(
                            "register-fail-exist-user",
                            pathParameters(
                                "provider" pathDescription "소셜 타입" example SocialType.values()
                                    .joinToString { value -> value.name.lowercase() }
                            ),
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "이미 가입한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday.toString()
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example TOKEN_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이지만, 이미 존재하는 닉네임이면") {
            val request = createRegisterRequest()
            every { authenticationService.register(request, TEST_PROVIDER) } throws ExistResourceException(
                EXIST_NICKNAME_ERROR_MESSAGE
            )
            it("409 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .post(targetUri, TEST_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent(request))
                )
                    .andExpect(status().isConflict)
                    .andDo(
                        createPathDocument(
                            "register-fail-exist-nickname",
                            pathParameters(
                                "provider" pathDescription "소셜 타입" example SocialType.values()
                                    .joinToString { value -> value.name.lowercase() }
                            ),
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "이미 존재하는 사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday.toString()
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example TOKEN_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이지만, 유효하지 않은 형식의 이메일이면") {
            val request = createInvalidEmailRegisterRequest()
            it("400 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .post(targetUri, TEST_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        createPathDocument(
                            "register-fail-invalid-email",
                            pathParameters(
                                "provider" pathDescription "소셜 타입" example SocialType.values()
                                    .joinToString { value -> value.name.lowercase() }
                            ),
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "올바르지 않은 형식의 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday.toString()
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example TOKEN_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이지만, 유효하지 않은 형식의 전화번호이면") {
            val request = createInvalidPhoneNumberRegisterRequest()
            it("400 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .post(targetUri, TEST_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        createPathDocument(
                            "register-fail-invalid-phone-number",
                            pathParameters(
                                "provider" pathDescription "소셜 타입" example SocialType.values()
                                    .joinToString { value -> value.name.lowercase() }
                            ),
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효한 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "올바르지 않은 형식의 휴대전화" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday.toString()
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example TOKEN_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            val request = createRegisterRequest()
            every { authenticationService.register(request, TEST_PROVIDER) } throws InvalidTokenException(
                TOKEN_ERROR_MESSAGE
            )
            it("401 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .post(targetUri, TEST_PROVIDER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent(request))
                )
                    .andExpect(status().isUnauthorized)
                    .andDo(
                        createPathDocument(
                            "register-fail-invalid-token",
                            pathParameters(
                                "provider" pathDescription "소셜 타입" example SocialType.values()
                                    .joinToString { value -> value.name.lowercase() }
                            ),
                            requestBody(
                                "idToken" type JsonFieldType.STRING description "유효하지 않은 ID TOKEN" example request.idToken,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example request.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example request.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example request.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example Gender.values()
                                    .joinToString(),
                                "birthday" type JsonFieldType.ARRAY description "사용자 생일" example request.birthday.toString()
                            ),
                            responseBody("errorMessage" type JsonFieldType.STRING description "에러 메시지")
                        )
                    )
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
                            "value" parameterDescription "중복되지 않은 사용자 닉네임" example TEST_NICKNAME
                        )
                    )
                }
            }
        }

        context("중복인 닉네임이면") {
            every { authenticationService.validateNickname(TEST_NICKNAME) } throws ExistResourceException(
                EXIST_NICKNAME_ERROR_MESSAGE
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
                            "value" parameterDescription "중복된 사용자 닉네임" example TEST_NICKNAME
                        )
                    )
                }
            }
        }
    }

    afterEach {
        restDocumentation.afterTest()
    }
})
