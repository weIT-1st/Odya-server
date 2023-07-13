package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.weit.odya.service.PlaceSearchHistoryService
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_AGE_RANGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_AGE_RANGE
import kr.weit.odya.support.TEST_PLACE_SEARCH_TERM
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createListSearchTerm
import kr.weit.odya.support.createPlaceSearchRequest
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.example
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.isOptional
import kr.weit.odya.support.test.pathDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(PlaceSearchHistoryController::class)
class PlaceSearchHistoryControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val placeSearchHistoryService: PlaceSearchHistoryService,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/place-search-histories") {
            val targetUri = "/api/v1/place-search-histories"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                val request = createPlaceSearchRequest()
                every { placeSearchHistoryService.saveSearchHistory(TEST_PLACE_SEARCH_TERM, TEST_USER_ID) } just Runs
                it("201 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "create-place-search-history-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "searchTerm" type JsonFieldType.STRING description "검색어" example TEST_PLACE_SEARCH_TERM,
                            ),
                        )
                    }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                val request = createPlaceSearchRequest()
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "create-place-search-history-fail-not-registered-user",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "searchTerm" type JsonFieldType.STRING description "검색어" example TEST_PLACE_SEARCH_TERM,
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = createPlaceSearchRequest()
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "create-place-search-history-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "searchTerm" type JsonFieldType.STRING description "검색어" example TEST_PLACE_SEARCH_TERM,
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/place-search-histories/ranking") {
            val targetUri = "/api/v1/place-search-histories/ranking"
            val response = createListSearchTerm()
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                every { placeSearchHistoryService.getOverallRanking() } returns createListSearchTerm()
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "place-search-overall-ranking-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "[]" type JsonFieldType.ARRAY description "검색어" example response[0],
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 가입되지 않은 유저인 경우") {
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "place-search-overall-ranking-fail-not-registered-user",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "place-search-overall-ranking-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/place-search-histories/ranking/ageRange/{ageRange}") {
            val targetUri = "/api/v1/place-search-histories/ranking/ageRange/{ageRange}"
            val response = createListSearchTerm()
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                every { placeSearchHistoryService.getAgeRangeRanking(TEST_USER_ID, TEST_AGE_RANGE) } returns createListSearchTerm()
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_AGE_RANGE)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "place-search-age-rang-ranking-success",
                                pathParameters(
                                    "ageRange" pathDescription "연령대" example TEST_AGE_RANGE isOptional true,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "[]" type JsonFieldType.ARRAY description "검색어" example response[0],
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 연령대가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_INVALID_AGE_RANGE)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "place-search-age-rang-ranking-fail-invalid-age-range",
                                pathParameters(
                                    "ageRange" pathDescription "양수가 아닌 연령대" example TEST_INVALID_AGE_RANGE isOptional true,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 가입되지 않은 유저인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, null)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "place-search-age-rang-ranking-fail-not-registered-user",
                                pathParameters(
                                    "ageRange" pathDescription "연령대" example TEST_AGE_RANGE isOptional true,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_AGE_RANGE)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "place-search-age-rang-ranking-fail-invalid-token",
                                pathParameters(
                                    "ageRange" pathDescription "연령대" example TEST_AGE_RANGE isOptional true,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                responseBody(
                                    "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
