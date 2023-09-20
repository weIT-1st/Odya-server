package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.CommunityLikeService
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_NOT_EXISTS_COMMUNITY_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.example
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.pathDescription
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(CommunityLikeController::class)
class CommunityLikeControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val communityLikeService: CommunityLikeService,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/communities/{communityId}/likes") {
            val targetUri = "/api/v1/communities/{communityId}/likes"
            context("유효한 요청이 주어지는 경우") {
                every { communityLikeService.createCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID) } just runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "create-community-like-success",
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 커뮤니티 아이디가 주어지는 경우") {
                every {
                    communityLikeService.createCommunityLike(
                        TEST_NOT_EXISTS_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_NOT_EXISTS_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "create-community-like-not-found-community",
                                pathParameters(
                                    "communityId" pathDescription "존재하지 않는 커뮤니티 ID" example TEST_NOT_EXISTS_COMMUNITY_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("요청 사용자가 이미 좋아요를 누른 경우") {
                every {
                    communityLikeService.createCommunityLike(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws ExistResourceException("요청 사용자($TEST_USER_ID)가 이미 좋아요를 누른 게시글($TEST_COMMUNITY_ID)입니다.")
                it("409를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isConflict)
                        .andDo(
                            createPathDocument(
                                "create-community-like-already-exist",
                                pathParameters(
                                    "communityId" pathDescription "이미 좋아요를 누른 커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                it("401을 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "create-community-like-invalid-token",
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/communities/{communityId}/likes") {
            val targetUri = "/api/v1/communities/{communityId}/likes"
            context("유효한 요청이 주어지는 경우") {
                every { communityLikeService.deleteCommunityLike(TEST_COMMUNITY_ID, TEST_USER_ID) } just runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "delete-community-like-success",
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 커뮤니티 아이디가 주어지는 경우") {
                every {
                    communityLikeService.deleteCommunityLike(
                        TEST_NOT_EXISTS_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_NOT_EXISTS_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "delete-community-like-not-found-community",
                                pathParameters(
                                    "communityId" pathDescription "존재하지 않는 커뮤니티 ID" example TEST_NOT_EXISTS_COMMUNITY_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                it("401을 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "delete-community-like-invalid-token",
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 ID" example TEST_COMMUNITY_ID,
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
