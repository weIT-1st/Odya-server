package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.UserService
import kr.weit.odya.support.EXIST_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_NICKNAME_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createInformationRequest
import kr.weit.odya.support.createUserResponse
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(UserController::class)
class UserControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val userService: UserService
) : DescribeSpec({
    val restDocumentation = ManualRestDocumentation()
    val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

    beforeEach {
        restDocumentation.beforeTest(javaClass, it.name.testName)
    }

    describe("GET /api/v1/users/me") {
        val targetUri = "/api/v1/users/me"
        context("유효한 토큰이면서, 가입된 사용자인 경우") {
            val response = createUserResponse()
            every { userService.getInformation(TEST_USER_ID) } returns response
            it("200 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isOk() }
                }.andDo {
                    createDocument(
                        "get-my-information-success",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        responseBody(
                            "email" type STRING description "사용자 이메일" example response.email isOptional true,
                            "nickname" type STRING description "사용자 닉네임" example response.nickname,
                            "phoneNumber" type STRING description "사용자 전화번호" example response.phoneNumber isOptional true,
                            "gender" type STRING description "사용자 성별" example response.gender.name,
                            "birthday" type STRING description "사용자 생일" example response.birthday.toString(),
                            "socialType" type STRING description "사용자 소셜 타입" example response.socialType.name
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
            it("401 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "get-my-information-fail-not-registered-user",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }

        context("유효하지 않은 토큰이면") {
            it("401 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "get-my-information-fail-invalid-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }
    }

    describe("PATCH /api/v1/users/email") {
        val targetUri = "/api/v1/users/email"
        context("유효한 토큰이면서, 가입되지 않은 이메일인 경우") {
            every { userService.updateEmail(TEST_USER_ID, TEST_ID_TOKEN) } just runs
            it("204 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isNoContent() }
                }.andDo {
                    createDocument(
                        "update-email-success",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN  WITH VALID EMAIL"
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 인증된 이메일이 토큰에 없는 경우") {
            every { userService.updateEmail(TEST_USER_ID, TEST_ID_TOKEN) } throws NoSuchElementException(
                NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE
            )
            it("404 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isNotFound() }
                }.andDo {
                    createDocument(
                        "update-email-fail-not-exist-email-in-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITHOUT AUTHENTICATED EMAIL"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 가입되어 있는 이메일인 경우") {
            every { userService.updateEmail(TEST_USER_ID, TEST_ID_TOKEN) } throws ExistResourceException(
                EXIST_EMAIL_ERROR_MESSAGE
            )
            it("409 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isConflict() }
                }.andDo {
                    createDocument(
                        "update-email-fail-exist-email",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH EXIST EMAIL"
                        )
                    )
                }
            }
        }

        context("유효하지 않은 토큰이면") {
            it("401 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "update-email-fail-invalid-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }
    }

    describe("PATCH /api/v1/users/phone-number") {
        val targetUri = "/api/v1/users/phone-number"
        context("유효한 토큰이면서, 가입되지 않은 전화번호인 경우") {
            every { userService.updatePhoneNumber(TEST_USER_ID, TEST_ID_TOKEN) } just runs
            it("204 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isNoContent() }
                }.andDo {
                    createDocument(
                        "update-phone-number-success",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH VALID PHONE NUMBER"
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 인증된 전화번호가 토큰에 없는 경우") {
            every { userService.updatePhoneNumber(TEST_USER_ID, TEST_ID_TOKEN) } throws NoSuchElementException(
                NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE
            )
            it("404 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isNotFound() }
                }.andDo {
                    createDocument(
                        "update-phone-number-fail-not-exist-phone-number-in-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITHOUT AUTHENTICATED PHONE NUMBER"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 가입되어 있는 전화번호인 경우") {
            every { userService.updatePhoneNumber(TEST_USER_ID, TEST_ID_TOKEN) } throws ExistResourceException(
                EXIST_PHONE_NUMBER_ERROR_MESSAGE
            )
            it("409 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isConflict() }
                }.andDo {
                    createDocument(
                        "update-phone-number-fail-exist-email",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH EXIST PHONE NUMBER"
                        )
                    )
                }
            }
        }

        context("유효하지 않은 토큰이면") {
            it("401 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "update-phone-number-fail-invalid-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }
    }

    describe("PATCH /api/v1/users/information") {
        val targetUri = "/api/v1/users/information"
        context("유효한 토큰이면서, 유효한 정보인 경우") {
            val informationRequest = createInformationRequest()
            every { userService.updateInformation(TEST_USER_ID, informationRequest) } just runs
            it("204 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(informationRequest)
                }.andExpect {
                    status { isNoContent() }
                }.andDo {
                    createDocument(
                        "update-information-success",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "nickname" type STRING description "닉네임" example informationRequest.nickname
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 중복된 닉네임인 경우") {
            val informationRequest = createInformationRequest()
            every { userService.updateInformation(TEST_USER_ID, informationRequest) } throws ExistResourceException(
                EXIST_NICKNAME_ERROR_MESSAGE
            )
            it("409 응답한다.") {
                restDocMockMvc.patch(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(informationRequest)
                }.andExpect {
                    status { isConflict() }
                }.andDo {
                    createDocument(
                        "update-information-fail-exist-nickname",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "nickname" type STRING description "중복된 닉네임" example informationRequest.nickname
                        )
                    )
                }
            }
        }

        context("유효하지 않은 토큰이면") {
            it("401 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "update-information-fail-invalid-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                        ),
                        responseBody(
                            "errorMessage" type STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
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
