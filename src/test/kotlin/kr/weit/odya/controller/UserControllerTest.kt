package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import kr.weit.odya.service.UserService
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.TOKEN_ERROR_MESSAGE
import kr.weit.odya.support.createUserResponse
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.get
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
                            "email" type JsonFieldType.STRING description "사용자 이메일" example response.email isOptional true,
                            "nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.nickname,
                            "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example response.phoneNumber isOptional true,
                            "gender" type JsonFieldType.STRING description "사용자 성별" example response.gender.name,
                            "birthday" type JsonFieldType.STRING description "사용자 생일" example response.birthday.toString(),
                            "socialType" type JsonFieldType.STRING description "사용자 소셜 타입" example response.socialType.name
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
            every { userService.getInformation(TEST_USER_ID) } throws IllegalArgumentException("$TEST_USERNAME: 사용자가 존재하지 않습니다")
            it("400 응답한다.") {
                restDocMockMvc.get(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                }.andExpect {
                    status { isBadRequest() }
                }.andDo {
                    createDocument(
                        "get-my-information-fail-not-registered-user",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        responseBody(
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example TOKEN_ERROR_MESSAGE
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
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example TOKEN_ERROR_MESSAGE
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
