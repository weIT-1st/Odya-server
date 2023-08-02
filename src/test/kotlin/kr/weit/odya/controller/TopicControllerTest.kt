/*
package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import kr.weit.odya.service.TopicService
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.createTopicList
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.example
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.isOptional
import kr.weit.odya.support.test.parameterDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(TopicController::class)
class TopicControllerTest(
    @MockkBean
    private val topicService: TopicService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("GET /api/v1/topics/list") {
            val targetUri = "/api/v1/topics/list"
            context("유효한 요청 데이터가 전달되면") {
                val response = createTopicList()
                val content = response[0]
                every { topicService.getTopicList() } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "topic-get-list-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                    "" type JsonFieldType.NUMBER description "토픽 ID" example content.id,
                                    "content[].word" type JsonFieldType.STRING description "토픽 단어" example content.word,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 조회할 size가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "topic-get-list-fail-invalid-size",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수(default = 11)" example TEST_INVALID_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 조회할 마지막 ID가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_DEFAULT_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "topic-get-list-fail-invalid-last-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수(default = 11)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_DEFAULT_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "topic-get-list-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 11)" example TEST_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
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
*/
