package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.InterestPlaceService
import kr.weit.odya.support.EXIST_INTEREST_PLACE_ERROR_MESSAGE
import kr.weit.odya.support.NOT_FOUND_INTEREST_PLACE_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_EXIST_INTEREST_PLACE_ID
import kr.weit.odya.support.TEST_INTEREST_PLACE_ID
import kr.weit.odya.support.TEST_INVALID_INTEREST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createInterestPlaceRequest
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.example
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.pathDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
                it("401를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
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

        describe("DELETE /api/v1/interest-places/{id}") {
            val targetUri = "/api/v1/interest-places/{id}"
            context("유효한 요청 데이터가 전달되면") {
                every { interestPlaceService.deleteInterestPlace(TEST_USER_ID, TEST_INTEREST_PLACE_ID) } just Runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INTEREST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    ).andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "interest-place-delete-success",
                                pathParameters(
                                    "id" pathDescription "관심 장소 ID" example TEST_INTEREST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("양수가 아닌 관심 장소 ID인 경우") {
                it("400를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INVALID_INTEREST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    ).andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "interest-place-delete-failed-invalid-interest-place-id",
                                pathParameters(
                                    "id" pathDescription "양수가 아닌 관심 장소 ID" example TEST_INVALID_INTEREST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INTEREST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                    ).andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "interest-place-delete-failed-not-exist-user-id",
                                pathParameters(
                                    "id" pathDescription "관심 장소 ID" example TEST_INTEREST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만, 관심 장소가 아닌 경우") {
                every { interestPlaceService.deleteInterestPlace(TEST_USER_ID, TEST_EXIST_INTEREST_PLACE_ID) } throws NoSuchElementException(NOT_FOUND_INTEREST_PLACE_ERROR_MESSAGE)
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_EXIST_INTEREST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    ).andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "interest-place-delete-failed-not-exist-interest-place-id",
                                pathParameters(
                                    "id" pathDescription "존재하지 않는 관심 장소 ID" example TEST_EXIST_INTEREST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INTEREST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    ).andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "interest-place-delete-failed-invalid-token",
                                pathParameters(
                                    "id" pathDescription "관심 장소 ID" example TEST_INTEREST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/interest-places/{placeId}") {
            val targetUri = "/api/v1/interest-places/{placeId}"
            context("유효한 요청 데이터가 전달되면") {
                every { interestPlaceService.getInterestPlace(TEST_USER_ID, TEST_PLACE_ID) } returns true
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    ).andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "interest-place-check-success",
                                pathParameters(
                                    "placeId" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰 전달되면") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    ).andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "interest-place-check-failed-invalid-token",
                                pathParameters(
                                    "placeId" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
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
