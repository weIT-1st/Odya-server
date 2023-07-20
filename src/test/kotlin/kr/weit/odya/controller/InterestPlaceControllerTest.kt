package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.InterestPlaceService
import kr.weit.odya.support.EXIST_INTEREST_PLACE_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createInterestPlaceRequest
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(InterestPlaceController::class)
class InterestPlaceControllerTest(
    @MockkBean private val interestPlaceService: InterestPlaceService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/interest-places") {
            val targetUri = "/api/v1/interest-places"
            context("유효한 요청 데이터가 전달되면") {
                val request = createInterestPlaceRequest()
                every { interestPlaceService.createInterestPlace(TEST_USER_ID, createInterestPlaceRequest()) } just Runs
                it("201를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "interest-place-create-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서 장소 ID가 빈 문자열이면") {
                val request = createInterestPlaceRequest().copy(placeId = " ")
                it("400를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "interest-place-create-failed-empty-place-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "공백인 장소 ID" example """ """,
                            ),
                        )
                    }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                val request = createInterestPlaceRequest()
                every { interestPlaceService.createInterestPlace(TEST_USER_ID, createInterestPlaceRequest()) } throws NoSuchElementException()
                it("401를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_NOT_EXIST_USER_ID)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "interest-place-create-failed-not-exist-user-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 관심 장소인 경우") {
                val request = createInterestPlaceRequest()
                every { interestPlaceService.createInterestPlace(TEST_USER_ID, createInterestPlaceRequest()) } throws ExistResourceException(EXIST_INTEREST_PLACE_ERROR_MESSAGE)
                it("409를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "interest-place-create-failed-not-exist-user-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = createInterestPlaceRequest()
                it("401를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "interest-place-create-failed-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                            ),
                        )
                    }
                }
            }
        }
        afterEach {
            restDocumentation.afterTest()
        }
    },
)
