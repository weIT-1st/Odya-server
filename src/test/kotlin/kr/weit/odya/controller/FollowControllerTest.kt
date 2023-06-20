package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.FollowService
import kr.weit.odya.support.ALREADY_FOLLOW_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_USER_ID
import kr.weit.odya.support.TEST_PAGE
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFollowCountsResponse
import kr.weit.odya.support.createFollowRequest
import kr.weit.odya.support.createFollowUserPage
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
import kr.weit.odya.support.test.parameterDescription
import kr.weit.odya.support.test.pathDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(FollowController::class)
class FollowControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val followService: FollowService
) : DescribeSpec({
    val restDocumentation = ManualRestDocumentation()
    val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

    beforeEach {
        restDocumentation.beforeTest(javaClass, it.name.testName)
    }

    describe("POST /api/v1/follows") {
        val targetUri = "/api/v1/follows"
        context("유효한 토큰이면서, 유효한 요청인 경우") {
            val request = createFollowRequest()
            every { followService.createFollow(TEST_USER_ID, request) } just runs
            it("201 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isCreated() }
                }.andDo {
                    createDocument(
                        "create-follow-success",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "팔로우를 신청할 USER ID" example request.followingId
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 음수의 FOLLOWING ID인 경우") {
            val request = createFollowRequest().copy(TEST_INVALID_USER_ID)
            it("400 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isBadRequest() }
                }.andDo {
                    createDocument(
                        "create-follow-fail-request-resource-negative",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "음수의 FOLLOWING ID" example request.followingId
                        ),
                        responseBody(
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, 이미 팔로우 중인 FOLLOWING ID인 경우") {
            val request = createFollowRequest()
            every { followService.createFollow(TEST_USER_ID, request) } throws ExistResourceException(
                ALREADY_FOLLOW_ERROR_MESSAGE
            )
            it("409 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isConflict() }
                }.andDo {
                    createDocument(
                        "create-follow-fail-already-following",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "이미 팔로우된 USER ID" example request.followingId
                        ),
                        responseBody(
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            val request = createFollowRequest()
            it("401 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "create-follow-fail-invalid-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "팔로우를 신청할 USER ID" example request.followingId
                        ),
                        responseBody(
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }
    }

    describe("DELETE /api/v1/follows") {
        val targetUri = "/api/v1/follows"
        context("유효한 토큰이면서, 유효한 요청인 경우") {
            val request = createFollowRequest()
            every { followService.deleteFollow(TEST_USER_ID, request) } just runs
            it("204 응답한다.") {
                restDocMockMvc.delete(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isNoContent() }
                }.andDo {
                    createDocument(
                        "delete-follow-success",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "팔로우를 취소할 USER ID" example request.followingId
                        )
                    )
                }
            }
        }

        context("유효한 토큰이면서, FOLLOWING ID가 음수인 경우") {
            val request = createFollowRequest().copy(TEST_INVALID_USER_ID)
            it("400 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isBadRequest() }
                }.andDo {
                    createDocument(
                        "delete-follow-fail-request-resource-negative",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "음수의 FOLLOWING ID" example request.followingId
                        ),
                        responseBody(
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            val request = createFollowRequest()
            it("401 응답한다.") {
                restDocMockMvc.post(targetUri) {
                    header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    jsonContent(request)
                }.andExpect {
                    status { isUnauthorized() }
                }.andDo {
                    createDocument(
                        "delete-follow-fail-invalid-token",
                        requestHeaders(
                            HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                        ),
                        requestBody(
                            "followingId" type JsonFieldType.NUMBER description "팔로우를 취소할 USER ID" example request.followingId
                        ),
                        responseBody(
                            "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                        )
                    )
                }
            }
        }
    }

    describe("GET /api/v1/follows/{userId}/counts") {
        val targetUri = "/api/v1/follows/{userId}/counts"
        context("유효한 토큰이면서, 유효한 요청인 경우") {
            val response = createFollowCountsResponse()
            every { followService.getFollowCounts(TEST_USER_ID) } returns response
            it("200 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        createPathDocument(
                            "get-follow-counts-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로워/팔로잉 수를 조회할 USER ID" example TEST_USER_ID
                            ),
                            responseBody(
                                "followingCount" type JsonFieldType.NUMBER description "팔로잉 수" example response.followingCount,
                                "followerCount" type JsonFieldType.NUMBER description "팔로워 수" example response.followerCount
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이면서, USER ID가 음수인 경우") {
            it("400 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_INVALID_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        createPathDocument(
                            "get-follow-counts-fail-user-id-negative",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            it("401 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                )
                    .andExpect(status().isUnauthorized)
                    .andDo(
                        createPathDocument(
                            "get-follow-counts-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로워/팔로잉 수를 조회할 USER ID" example TEST_USER_ID
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }
    }

    describe("GET /api/v1/follows/{userId}/followings") {
        val targetUri = "/api/v1/follows/{userId}/followings"
        context("유효한 토큰이면서, 유효한 요청인 경우") {
            val response = createFollowUserPage()
            every { followService.getFollowings(TEST_USER_ID, any()) } returns response
            it("200, 전체 팔로잉 목록을 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        createPathDocument(
                            "get-followings-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID
                            ),
                            responseBody(
                                "page" type JsonFieldType.NUMBER description "현재 페이지 번호" example response.page,
                                "totalPages" type JsonFieldType.NUMBER description "전체 페이지 번호" example response.totalPages,
                                "totalElements" type JsonFieldType.NUMBER description "전체 데이터 수" example response.totalElements,
                                "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].userId,
                                "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].nickname,
                                "content[].profileName" type JsonFieldType.STRING description "사용자 프로필" example response.content[0].profileName
                            )
                        )
                    )
            }

            it("200, 페이지 단위 팔로잉 목록을 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        .param("page", TEST_PAGE.toString())
                        .param("size", TEST_SIZE.toString())
                )
                    .andExpect(status().isOk)
                    .andDo(
                        createPathDocument(
                            "get-following-page-success-with-page",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID
                            ),
                            queryParameters(
                                "page" parameterDescription "조회할 페이지 번호" isOptional true example TEST_PAGE,
                                "size" parameterDescription "조회할 페이지 사이즈" isOptional true example TEST_SIZE
                            ),
                            responseBody(
                                "page" type JsonFieldType.NUMBER description "현재 페이지 번호" example response.page,
                                "totalPages" type JsonFieldType.NUMBER description "전체 페이지 번호" example response.totalPages,
                                "totalElements" type JsonFieldType.NUMBER description "전체 데이터 수" example response.totalElements,
                                "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].userId,
                                "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].nickname,
                                "content[].profileName" type JsonFieldType.STRING description "사용자 프로필" example response.content[0].profileName
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이면서, USER ID가 음수인 경우") {
            it("400 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_INVALID_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        createPathDocument(
                            "get-following-page-fail-invalid-user-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            it("401 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                )
                    .andExpect(status().isUnauthorized)
                    .andDo(
                        createPathDocument(
                            "get-following-page-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }
    }

    describe("GET /api/v1/follows/{userId}/followers") {
        val targetUri = "/api/v1/follows/{userId}/followers"
        context("유효한 토큰이면서, 유효한 요청인 경우") {
            val response = createFollowUserPage()
            every { followService.getFollowers(TEST_USER_ID, any()) } returns response
            it("200, 전체 팔로워 목록을 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        createPathDocument(
                            "get-followers-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로워 목록을 조회할 USER ID" example TEST_USER_ID
                            ),
                            responseBody(
                                "page" type JsonFieldType.NUMBER description "현재 페이지 번호" example response.page,
                                "totalPages" type JsonFieldType.NUMBER description "전체 페이지 번호" example response.totalPages,
                                "totalElements" type JsonFieldType.NUMBER description "전체 데이터 수" example response.totalElements,
                                "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].userId,
                                "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].nickname,
                                "content[].profileName" type JsonFieldType.STRING description "사용자 프로필" example response.content[0].profileName
                            )
                        )
                    )
            }

            it("200, 페이지 단위 팔로워 목록을 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        .param("page", TEST_PAGE.toString())
                        .param("size", TEST_SIZE.toString())
                )
                    .andExpect(status().isOk)
                    .andDo(
                        createPathDocument(
                            "get-followers-page-success-with-page",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로워 목록을 조회할 USER ID" example TEST_USER_ID
                            ),
                            queryParameters(
                                "page" parameterDescription "조회할 페이지 번호" isOptional true example TEST_PAGE,
                                "size" parameterDescription "조회할 페이지 사이즈" isOptional true example TEST_SIZE
                            ),
                            responseBody(
                                "page" type JsonFieldType.NUMBER description "현재 페이지 번호" example response.page,
                                "totalPages" type JsonFieldType.NUMBER description "전체 페이지 번호" example response.totalPages,
                                "totalElements" type JsonFieldType.NUMBER description "전체 데이터 수" example response.totalElements,
                                "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].userId,
                                "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].nickname,
                                "content[].profileName" type JsonFieldType.STRING description "사용자 프로필" example response.content[0].profileName
                            )
                        )
                    )
            }
        }

        context("유효한 토큰이면서, USER ID가 음수인 경우") {
            it("400 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_INVALID_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        createPathDocument(
                            "get-follower-page-fail-invalid-user-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }

        context("유효하지 않은 토큰이 전달되면") {
            it("401 응답한다.") {
                restDocMockMvc.perform(
                    RestDocumentationRequestBuilders
                        .get(targetUri, TEST_USER_ID)
                        .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                )
                    .andExpect(status().isUnauthorized)
                    .andDo(
                        createPathDocument(
                            "get-follower-page-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN"
                            ),
                            pathParameters(
                                "userId" pathDescription "팔로워 목록을 조회할 USER ID" example TEST_USER_ID
                            ),
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE
                            )
                        )
                    )
            }
        }
    }

    afterEach {
        restDocumentation.afterTest()
    }
})
