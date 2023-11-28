package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.service.CommunityCommentService
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_COMMUNITY_COMMENT_ID
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_NOT_EXISTS_COMMUNITY_ID
import kr.weit.odya.support.TEST_OTHER_COMMUNITY_COMMENT_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunityCommentRequest
import kr.weit.odya.support.createCommunityCommentResponse
import kr.weit.odya.support.createCommunityCommentSliceResponse
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
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
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(CommunityCommentController::class)
class CommunityCommentControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val communityCommentService: CommunityCommentService,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/communities/{communityId}/comments") {
            val targetUri = "/api/v1/communities/{communityId}/comments"
            context("유효한 정보가 주어졌을 때") {
                val request = createCommunityCommentRequest()
                every {
                    communityCommentService.createCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        request,
                    )
                } returns
                    TEST_COMMUNITY_COMMENT_ID
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isCreated)
                        .andDo(
                            createPathDocument(
                                "comment-create-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 커뮤니티 ID가 주어졌을 때") {
                val request = createCommunityCommentRequest()
                every {
                    communityCommentService.createCommunityComment(
                        TEST_USER_ID,
                        TEST_NOT_EXISTS_COMMUNITY_ID,
                        request,
                    )
                } throws NoSuchElementException("$TEST_NOT_EXISTS_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_NOT_EXISTS_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "comment-create-fail-not-exists-community",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "존재하지 않는 커뮤니티 ID" example TEST_NOT_EXISTS_COMMUNITY_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어졌을 때") {
                val request = createCommunityCommentRequest()
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "comment-create-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/communities/{communityId}/comments") {
            val targetUri = "/api/v1/communities/{communityId}/comments"
            context("유효한 정보가 주어졌을 때 (lastId null)") {
                val response = createCommunityCommentSliceResponse()
                every { communityCommentService.getCommunityComments(TEST_USER_ID, TEST_COMMUNITY_ID, 10, null) } returns
                    response
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "comment-get-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지가 있는지 여부" example response.hasNext,
                                    "content[].communityCommentId" type JsonFieldType.NUMBER description "댓글 ID" example response.content[0].communityCommentId,
                                    "content[].content" type JsonFieldType.STRING description "댓글 내용" example response.content[0].content,
                                    "content[].updatedAt" type JsonFieldType.STRING description "댓글 수정 시각" example response.content[0].updatedAt,
                                    "content[].isWriter" type JsonFieldType.BOOLEAN description "댓글 작성자 여부" example response.content[0].isWriter,
                                    "content[].user.userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].user.userId,
                                    "content[].user.nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].user.nickname,
                                    "content[].user.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].user.isFollowing,
                                    "content[].user.profile.profileUrl" type JsonFieldType.STRING description "프로필 URL" example response.content[0].user.profile.profileUrl,
                                    "content[].user.profile.profileColor.colorHex" type JsonFieldType.STRING description "프로필 색상 HEX" example response.content[0].user.profile.profileColor?.colorHex,
                                    "content[].user.profile.profileColor.red" type JsonFieldType.NUMBER description "프로필 색상 RED" example response.content[0].user.profile.profileColor?.red,
                                    "content[].user.profile.profileColor.green" type JsonFieldType.NUMBER description "프로필 색상 GREEN" example response.content[0].user.profile.profileColor?.green,
                                    "content[].user.profile.profileColor.blue" type JsonFieldType.NUMBER description "프로필 색상 BLUE" example response.content[0].user.profile.profileColor?.blue,
                                ),
                            ),
                        )
                }
            }

            context("lastId와 함께 유효한 정보가 주어졌을 때") {
                val response = SliceResponse(
                    TEST_DEFAULT_SIZE,
                    listOf(
                        createCommunityCommentResponse(
                            id = TEST_OTHER_COMMUNITY_COMMENT_ID,
                            user = createOtherUser(),
                        ),
                    ),
                )
                every { communityCommentService.getCommunityComments(TEST_USER_ID, TEST_COMMUNITY_ID, 10, TEST_COMMUNITY_ID) } returns
                    response
                it("200를 반환한다.") {
                    restDocMockMvc.perform(
                        get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param("lastId", TEST_COMMUNITY_ID.toString()),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "comment-get-success-with-last-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_COMMUNITY_ID isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지가 있는지 여부" example response.hasNext,
                                    "content[].communityCommentId" type JsonFieldType.NUMBER description "댓글 ID" example response.content[0].communityCommentId,
                                    "content[].content" type JsonFieldType.STRING description "댓글 내용" example response.content[0].content,
                                    "content[].updatedAt" type JsonFieldType.STRING description "댓글 수정 시각" example response.content[0].updatedAt,
                                    "content[].isWriter" type JsonFieldType.BOOLEAN description "댓글 작성자 여부" example response.content[0].isWriter,
                                    "content[].user.userId" type JsonFieldType.NUMBER description "사용자 ID" example response.content[0].user.userId,
                                    "content[].user.nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.content[0].user.nickname,
                                    "content[].user.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].user.isFollowing,
                                    "content[].user.profile.profileUrl" type JsonFieldType.STRING description "프로필 URL" example response.content[0].user.profile.profileUrl,
                                    "content[].user.profile.profileColor.colorHex" type JsonFieldType.STRING description "프로필 색상 HEX" example response.content[0].user.profile.profileColor?.colorHex,
                                    "content[].user.profile.profileColor.red" type JsonFieldType.NUMBER description "프로필 색상 RED" example response.content[0].user.profile.profileColor?.red,
                                    "content[].user.profile.profileColor.green" type JsonFieldType.NUMBER description "프로필 색상 GREEN" example response.content[0].user.profile.profileColor?.green,
                                    "content[].user.profile.profileColor.blue" type JsonFieldType.NUMBER description "프로필 색상 BLUE" example response.content[0].user.profile.profileColor?.blue,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어졌을 때") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "comment-get-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }
        }

        describe("PATCH /api/v1/communities/{communityId}/comments/{commentId}") {
            val targetUri = "/api/v1/communities/{communityId}/comments/{commentId}"
            context("유효한 정보가 주어졌을 때") {
                val request = createCommunityCommentRequest()
                every {
                    communityCommentService.updateCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_COMMUNITY_COMMENT_ID,
                        request,
                    )
                } just runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        patch(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "comment-update-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "수정할 댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }

            context("커뮤니티 ID와 커뮤니티 댓글 ID에 일치하는 커뮤니티 댓글이 없을 때") {
                val request = createCommunityCommentRequest()
                every {
                    communityCommentService.updateCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_COMMUNITY_COMMENT_ID,
                        request,
                    )
                } throws NoSuchElementException("존재하지 않는 커뮤니티 댓글입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        patch(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "comment-update-fail-not-exists-community",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "수정할 댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }

            context("커뮤니티 댓글 작성자와 다른 사용자가 수정을 시도할 때") {
                val request = createCommunityCommentRequest()
                every {
                    communityCommentService.updateCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_COMMUNITY_COMMENT_ID,
                        request,
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 커뮤니티 댓글($TEST_COMMUNITY_COMMENT_ID)을 처리할 권한이 없습니다.")
                it("403를 반환한다.") {
                    restDocMockMvc.perform(
                        patch(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "comment-update-fail-not-permitted-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "댓글 작성자와 다른 사용자의 VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "수정할 댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어졌을 때") {
                val request = createCommunityCommentRequest()
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        patch(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonContent(request)),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "comment-update-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                                requestBody(
                                    "content" type JsonFieldType.STRING description "수정할 댓글 내용" example request.content,
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/communities/{communityId}/comments/{commentId}") {
            val targetUri = "/api/v1/communities/{communityId}/comments/{commentId}"
            context("유효한 정보가 주어졌을 때") {
                every {
                    communityCommentService.deleteCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_COMMUNITY_COMMENT_ID,
                    )
                } just runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "comment-delete-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                            ),
                        )
                }
            }

            context("커뮤니티 ID와 커뮤니티 댓글 ID에 일치하는 커뮤니티 댓글이 없을 때") {
                every {
                    communityCommentService.deleteCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_COMMUNITY_COMMENT_ID,
                    )
                } throws NoSuchElementException("존재하지 않는 커뮤니티 댓글입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "comment-delete-fail-not-exists-community",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                            ),
                        )
                }
            }

            context("커뮤니티 댓글 작성자와 다른 사용자가 수정을 시도할 때") {
                every {
                    communityCommentService.deleteCommunityComment(
                        TEST_USER_ID,
                        TEST_COMMUNITY_ID,
                        TEST_COMMUNITY_COMMENT_ID,
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 커뮤니티 댓글($TEST_COMMUNITY_COMMENT_ID)을 처리할 권한이 없습니다.")
                it("403를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "comment-delete-fail-not-permitted-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "댓글 작성자와 다른 사용자의 VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어졌을 때") {
                it("401를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_COMMUNITY_ID, TEST_COMMUNITY_COMMENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "comment-delete-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                    "commentId" pathDescription "댓글 ID" example TEST_COMMUNITY_COMMENT_ID,
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
