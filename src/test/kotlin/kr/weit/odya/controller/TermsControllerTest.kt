package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import kr.weit.odya.service.TermsService
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_TERMS_ID
import kr.weit.odya.support.TEST_NOT_EXIST_TERMS_ID
import kr.weit.odya.support.TEST_TERMS_ID
import kr.weit.odya.support.createTermsContentResponse
import kr.weit.odya.support.createTermsListResponse
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(TermsController::class)
class TermsControllerTest(
    @MockkBean
    private val termsService: TermsService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("GET /api/v1/terms") {
            val targetUri = "/api/v1/terms"
            context("유효한 요청이 전달되면") {
                val response = createTermsListResponse()
                every { termsService.getTermsList() } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "terms-list-get-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "[].id" type JsonFieldType.NUMBER description "약관 ID" example response[0].id,
                                "[].title" type JsonFieldType.STRING description "약관 제목" example response[0].title,
                                "[].required" type JsonFieldType.NUMBER description "필수 여부" example response[0].required,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰 전달되면") {
                it("401를 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "terms-list-get-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/terms/{id}") {
            val targetUri = "/api/v1/terms/{id}"
            context("유효한 약관ID가 전달되면") {
                val response = createTermsContentResponse()
                every { termsService.getTermsContent(TEST_TERMS_ID) } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_TERMS_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "terms-list-get-success",
                                pathParameters(
                                    "id" pathDescription "약관 ID" example TEST_TERMS_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "id" type JsonFieldType.NUMBER description "약관 ID" example response.id,
                                    "content" type JsonFieldType.STRING description "약관 내용" example response.content,
                                ),
                            ),
                        )
                }
            }

            context("양수가 아닌 약관ID가 전달되면") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_TERMS_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "terms-get-fail-invalid-id",
                                pathParameters(
                                    "id" pathDescription "양수가 아닌 약관 ID" example TEST_INVALID_TERMS_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 약관ID가 전달되면") {
                every { termsService.getTermsContent(TEST_NOT_EXIST_TERMS_ID) } throws NoSuchElementException()
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_NOT_EXIST_TERMS_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "terms-get-fail-not-exist-id",
                                pathParameters(
                                    "id" pathDescription "존재하지 않는 약관 ID" example TEST_NOT_EXIST_TERMS_ID,
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
                            .get(targetUri, TEST_TERMS_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "terms-get-fail-invalid-token",
                                pathParameters(
                                    "id" pathDescription "약관 ID" example TEST_TERMS_ID,
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
