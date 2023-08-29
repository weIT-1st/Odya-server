package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.domain.follow.FollowSortType
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.FollowService
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.support.ALREADY_FOLLOW_ERROR_MESSAGE
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.NICKNAME_PARAM
import kr.weit.odya.support.PAGE_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.SORT_TYPE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_INVALID_USER_ID
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PAGE
import kr.weit.odya.support.TEST_PAGEABLE
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_SORT_TYPE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFollowCountsResponse
import kr.weit.odya.support.createFollowRequest
import kr.weit.odya.support.createFollowSlice
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(FollowController::class)
class FollowControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val followService: FollowService,
) : DescribeSpec(
    {
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
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "팔로우를 신청할 USER ID" example request.followingId,
                            ),
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
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "음수의 FOLLOWING ID" example request.followingId,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 이미 팔로우 중인 FOLLOWING ID인 경우") {
                val request = createFollowRequest()
                every { followService.createFollow(TEST_USER_ID, request) } throws ExistResourceException(
                    ALREADY_FOLLOW_ERROR_MESSAGE,
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
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "이미 팔로우된 USER ID" example request.followingId,
                            ),
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
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "팔로우를 신청할 USER ID" example request.followingId,
                            ),
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
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "팔로우를 취소할 USER ID" example request.followingId,
                            ),
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
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "음수의 FOLLOWING ID" example request.followingId,
                            ),
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
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "followingId" type JsonFieldType.NUMBER description "팔로우를 취소할 USER ID" example request.followingId,
                            ),
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
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "get-follow-counts-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로워/팔로잉 수를 조회할 USER ID" example TEST_USER_ID,
                                ),
                                responseBody(
                                    "followingCount" type JsonFieldType.NUMBER description "팔로잉 수" example response.followingCount,
                                    "followerCount" type JsonFieldType.NUMBER description "팔로워 수" example response.followerCount,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, USER ID가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-follow-counts-fail-user-id-negative",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-follow-counts-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로워/팔로잉 수를 조회할 USER ID" example TEST_USER_ID,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/follows/{userId}/followings") {
            val targetUri = "/api/v1/follows/{userId}/followings"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                val response = createFollowSlice()
                every {
                    followService.getSliceFollowings(TEST_USER_ID, TEST_PAGEABLE, TEST_SORT_TYPE)
                } returns response
                it("200, 팔로잉 목록을 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(PAGE_PARAM, TEST_PAGE.toString())
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_SORT_TYPE.name),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "get-following-slice-success-with-params",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                                queryParameters(
                                    PAGE_PARAM parameterDescription "데이터 조회 시작점 (default = 0)" example TEST_PAGE isOptional true,
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example FollowSortType.values()
                                        .joinToString() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                    "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].userId,
                                    "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].nickname,
                                    "content[].profile.profileUrl" type JsonFieldType.STRING description "사용자 프로필 Url" example response.content[0].profile.profileUrl,
                                    "content[].profile.profileColor.colorHex" type JsonFieldType.STRING description "색상 Hex" example response.content[0].profile.profileColor?.colorHex isOptional true,
                                    "content[].profile.profileColor.red" type JsonFieldType.NUMBER description "RGB RED" example response.content[0].profile.profileColor?.red isOptional true,
                                    "content[].profile.profileColor.green" type JsonFieldType.NUMBER description "RGB GREEN" example response.content[0].profile.profileColor?.green isOptional true,
                                    "content[].profile.profileColor.blue" type JsonFieldType.NUMBER description "RGB BLUE" example response.content[0].profile.profileColor?.blue isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 프로필 PreAuthentication Access Url 생성에 실패한 경우") {
                every {
                    followService.getSliceFollowings(TEST_USER_ID, TEST_PAGEABLE, TEST_SORT_TYPE)
                } throws ObjectStorageException(SOMETHING_ERROR_MESSAGE)
                it("500 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(PAGE_PARAM, TEST_PAGE.toString())
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_SORT_TYPE.name),
                    )
                        .andExpect(status().isInternalServerError)
                        .andDo(
                            createPathDocument(
                                "get-following-slice-fail-create-pre-auth-url",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                                queryParameters(
                                    PAGE_PARAM parameterDescription "데이터 조회 시작점 (default = 0)" example TEST_PAGE isOptional true,
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example FollowSortType.values()
                                        .joinToString() isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, USER ID가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-following-slice-fail-invalid-user-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-following-slice-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/follows/{userId}/followers") {
            val targetUri = "/api/v1/follows/{userId}/followers"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                val response = createFollowSlice()
                every {
                    followService.getSliceFollowers(TEST_USER_ID, TEST_PAGEABLE, TEST_SORT_TYPE)
                } returns response
                it("200, 팔로잉 목록을 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(PAGE_PARAM, TEST_PAGE.toString())
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_SORT_TYPE.name),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "get-follower-slice-success-with-params",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로워 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                                queryParameters(
                                    PAGE_PARAM parameterDescription "페이지 (default = 0)" example TEST_PAGE isOptional true,
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example FollowSortType.values()
                                        .joinToString() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                    "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].userId,
                                    "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].nickname,
                                    "content[].profile.profileUrl" type JsonFieldType.STRING description "사용자 프로필 Url" example response.content[0].profile.profileUrl,
                                    "content[].profile.profileColor.colorHex" type JsonFieldType.STRING description "색상 Hex" example response.content[0].profile.profileColor?.colorHex isOptional true,
                                    "content[].profile.profileColor.red" type JsonFieldType.NUMBER description "RGB RED" example response.content[0].profile.profileColor?.red isOptional true,
                                    "content[].profile.profileColor.green" type JsonFieldType.NUMBER description "RGB GREEN" example response.content[0].profile.profileColor?.green isOptional true,
                                    "content[].profile.profileColor.blue" type JsonFieldType.NUMBER description "RGB BLUE" example response.content[0].profile.profileColor?.blue isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 프로필 PreAuthentication Access Url 생성에 실패한 경우") {
                every {
                    followService.getSliceFollowers(TEST_USER_ID, TEST_PAGEABLE, TEST_SORT_TYPE)
                } throws ObjectStorageException(SOMETHING_ERROR_MESSAGE)
                it("500 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(PAGE_PARAM, TEST_PAGE.toString())
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_SORT_TYPE.name),
                    )
                        .andExpect(status().isInternalServerError)
                        .andDo(
                            createPathDocument(
                                "get-follower-slice-fail-create-pre-auth-url",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로잉 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                                queryParameters(
                                    PAGE_PARAM parameterDescription "데이터 조회 시작점 (default = 0)" example TEST_PAGE isOptional true,
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example FollowSortType.values()
                                        .joinToString() isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, USER ID가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-follower-slice-fail-invalid-user-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-follower-slice-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "팔로워 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/follows/search") {
            val targetUri = "/api/v1/follows/search"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createFollowSlice()
                every { followService.searchByNickname(TEST_USER_ID, TEST_NICKNAME, TEST_SIZE, TEST_LAST_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        val content = response.content[0]
                        createDocument(
                            "search-followings-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example "null" isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                NICKNAME_PARAM parameterDescription "검색할 닉네임" example TEST_NICKNAME,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example content.userId,
                                "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example content.nickname,
                                "content[].profile.profileUrl" type JsonFieldType.STRING description "사용자 프로필 Url" example content.profile.profileUrl,
                                "content[].profile.profileColor.colorHex" type JsonFieldType.STRING description "색상 Hex" example content.profile.profileColor?.colorHex isOptional true,
                                "content[].profile.profileColor.red" type JsonFieldType.NUMBER description "RGB RED" example content.profile.profileColor?.red isOptional true,
                                "content[].profile.profileColor.green" type JsonFieldType.NUMBER description "RGB GREEN" example content.profile.profileColor?.green isOptional true,
                                "content[].profile.profileColor.blue" type JsonFieldType.NUMBER description "RGB BLUE" example content.profile.profileColor?.blue isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 닉네임이 올바르지 않은 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, " ")
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-followings-fail-nickname-blank",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                NICKNAME_PARAM parameterDescription "올바르지 않은 닉네임" example " ",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 조회할 마지막 ID가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-followings-fail-invalid-last-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
                                NICKNAME_PARAM parameterDescription "검색할 닉네임" example TEST_NICKNAME,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 size가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-followings-fail-invalid-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                NICKNAME_PARAM parameterDescription "검색할 닉네임" example TEST_NICKNAME,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "search-followings-fail-not-registered-user",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "search-followings-fail-invalid-token",
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
