package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.service.TopicService
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_FAVORITE_TOPIC_ID
import kr.weit.odya.support.TEST_INVALID_FAVORITE_TOPIC_ID
import kr.weit.odya.support.TEST_NOT_EXIST_FAVORITE_TOPIC_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createAddFavoriteTopicRequest
import kr.weit.odya.support.createFavoriteTopicListResponse
import kr.weit.odya.support.createInvalidAddFavoriteTopicRequest
import kr.weit.odya.support.createTopicList
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
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
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
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

        describe("GET /api/v1/topics") {
            val targetUri = "/api/v1/topics"
            context("유효한 요청이 전달되면") {
                val response = createTopicList()
                every { topicService.getTopicList() } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "topic-get-list-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "[].id" type JsonFieldType.NUMBER description "토픽 ID" example response[0].id,
                                "[].word" type JsonFieldType.STRING description "토픽 단어" example response[0].word,
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
                            "topic-get-list-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/topics") {
            val targetUri = "/api/v1/topics"
            context("유효한 요청 데이터가 전달되면") {
                val request = createAddFavoriteTopicRequest()
                every { topicService.addFavoriteTopic(TEST_USER_ID, request) } returns Unit
                it("201를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "favorite-topic-add-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "topicIdList".type(JsonFieldType.ARRAY) description "토픽 ID 리스트" example request,
                            ),
                        )
                    }
                }
            }

            context("빈 리스트를 전달하면") {
                val request = listOf<Long>()
                it("400를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "favorite-topic-add-fail-empty-list",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "topicIdList".type(JsonFieldType.ARRAY) description "빈 토픽 ID 리스트" example request,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 존재하지않는 토픽 ID가 포함된 리스트를 전달하면") {
                val request = createInvalidAddFavoriteTopicRequest()
                every { topicService.addFavoriteTopic(TEST_USER_ID, request) } throws NoSuchElementException()
                it("404를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "favorite-topic-add-fail-not-found-topic",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "[]".type(JsonFieldType.ARRAY) description "존재하지않는 토픽 ID가 포함된 리스트" example request,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = createAddFavoriteTopicRequest()
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "favorite-topic-add-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("DELETE /api/v1/topics/{id}") {
            val targetUri = "/api/v1/topics/{id}"
            context("유효한 요청 데이터가 전달되면") {
                every { topicService.deleteFavoriteTopic(TEST_USER_ID, TEST_FAVORITE_TOPIC_ID) } returns Unit
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_FAVORITE_TOPIC_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "favorite-topic-delete-success",
                                RequestDocumentation.pathParameters(
                                    "id" pathDescription "관심 토픽 ID" example TEST_FAVORITE_TOPIC_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("양수가 아닌 관심 토픽 ID가 전달되면") {
                it("400를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INVALID_FAVORITE_TOPIC_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "favorite-topic-delete-fail-invalid-topic-id",
                                RequestDocumentation.pathParameters(
                                    "id" pathDescription "양수가 아닌 관심 토픽 ID" example TEST_INVALID_FAVORITE_TOPIC_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 관심 토픽 ID가 전달되면") {
                every { topicService.deleteFavoriteTopic(TEST_USER_ID, TEST_NOT_EXIST_FAVORITE_TOPIC_ID) } throws NoSuchElementException()
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_NOT_EXIST_FAVORITE_TOPIC_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "favorite-topic-delete-fail-not-found-id",
                                RequestDocumentation.pathParameters(
                                    "id" pathDescription "존재하지 않는 관심 토픽 ID" example TEST_NOT_EXIST_FAVORITE_TOPIC_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("삭제할 권한이 없는 userId가 전달되면") {
                every { topicService.deleteFavoriteTopic(TEST_USER_ID, TEST_FAVORITE_TOPIC_ID) } throws ForbiddenException()
                it("403를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_FAVORITE_TOPIC_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "favorite-topic-delete-fail-no-permissions",
                                RequestDocumentation.pathParameters(
                                    "id" pathDescription "관심 토픽 ID" example TEST_FAVORITE_TOPIC_ID,
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
                            .delete(targetUri, TEST_FAVORITE_TOPIC_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "favorite-topic-delete-fail-invalid-token",
                                RequestDocumentation.pathParameters(
                                    "id" pathDescription "관심 토픽 ID" example TEST_FAVORITE_TOPIC_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/topics/favorite") {
            val targetUri = "/api/v1/topics/favorite"
            context("유효한 요청이 전달되면") {
                val response = createFavoriteTopicListResponse()
                every { topicService.getFavoriteTopicList(TEST_USER_ID) } returns response
                it("200를 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "favorite-topic-list-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "[].id" type JsonFieldType.NUMBER description "관심 토픽 ID" example response[0].id,
                                "[].userId" type JsonFieldType.NUMBER description "유저 ID" example response[0].userId,
                                "[].topic.id" type JsonFieldType.NUMBER description "토픽 ID" example response[0].topic.id,
                                "[].topic.word" type JsonFieldType.STRING description "토픽 단어" example response[0].topic.word,
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
                            "favorite-topic-list-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
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
