package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkSortType
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.TravelJournalBookmarkService
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SORT_TYPE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_INVALID_USER_ID
import kr.weit.odya.support.TEST_NOT_EXIST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createSliceTravelJournalBookmarkSummaryResponse
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.generateRestDocMockMvc
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(TravelJournalBookmarkController::class)
class TravelJournalBookmarkControllerTest(
    @MockkBean private val travelJournalBookmarkService: TravelJournalBookmarkService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/travel-journal-bookmarks/{travelJournalId}") {
            val targetUri = "/api/v1/travel-journal-bookmarks/{travelJournalId}"
            context("유효한 요청이 주어지는 경우") {
                every {
                    travelJournalBookmarkService.createTravelJournalBookmark(
                        TEST_USER_ID,
                        TEST_TRAVEL_JOURNAL_ID,
                    )
                } just runs
                it("201를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isCreated)
                        .andDo(
                            createPathDocument(
                                "create-travel-journal-bookmark",
                                pathParameters(
                                    "travelJournalId" pathDescription "여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 여행일지 아이디가 주어지는 경우") {
                every {
                    travelJournalBookmarkService.createTravelJournalBookmark(
                        TEST_USER_ID,
                        TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                    )
                } throws NoSuchElementException("$TEST_NOT_EXIST_TRAVEL_JOURNAL_ID: 존재하지 않는 커뮤니티입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_NOT_EXIST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "create-travel-journal-bookmark-not-exist",
                                pathParameters(
                                    "travelJournalId" pathDescription "존재하지 않는 여행일지 ID" example TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }

                context("이미 여행일지 즐겨찾기를 눌렀을 경우") {
                    every {
                        travelJournalBookmarkService.createTravelJournalBookmark(
                            TEST_USER_ID,
                            TEST_TRAVEL_JOURNAL_ID,
                        )
                    } throws ExistResourceException("이미 사용자($TEST_USER_ID)가 즐겨찾기한 여행일지($TEST_TRAVEL_JOURNAL_ID) 입니다.")
                    it("409를 반환한다.") {
                        restDocMockMvc.perform(
                            post(targetUri, TEST_TRAVEL_JOURNAL_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        )
                            .andExpect(status().isConflict)
                            .andDo(
                                createPathDocument(
                                    "create-travel-journal-bookmark-already-exist",
                                    pathParameters(
                                        "travelJournalId" pathDescription "이미 즐겨찾기한 여행일지 ID" example TEST_TRAVEL_JOURNAL_ID,
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
                            post(targetUri, TEST_TRAVEL_JOURNAL_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                        )
                            .andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "create-travel-journal-bookmark-invalid-token",
                                    pathParameters(
                                        "travelJournalId" pathDescription "여행일지 ID" example TEST_TRAVEL_JOURNAL_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journal-bookmarks/me") {
            val targetUri = "/api/v1/travel-journal-bookmarks/me"
            context("유효한 요청이 주어지는 경우") {
                val response = createSliceTravelJournalBookmarkSummaryResponse()
                every {
                    travelJournalBookmarkService.getMyTravelJournalBookmarks(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-my-travel-journal-bookmarks",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID(travelJournalBookmarkId 값)" example "null" isOptional true,
                                SORT_TYPE_PARAM parameterDescription "정렬 타입" example TravelJournalBookmarkSortType.values() isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].travelJournalBookmarkId" type JsonFieldType.NUMBER description "여행 일지 즐겨찾기 아이디" example response.content[0].travelJournalBookmarkId,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].travelJournalMainImageUrl" type JsonFieldType.STRING description "여행 일지의 대표 이미지 URL" example response.content[0].travelJournalMainImageUrl,
                                "content[].isBookmarked" type JsonFieldType.BOOLEAN description "사용자가 즐겨찾기한 여행일지 여부" example response.content[0].isBookmarked,
                                "content[].isRepresentative" type JsonFieldType.BOOLEAN description "여행 일지가 대표 여행일지인지 여부" example response.content[0].isRepresentative,
                                "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                it("401을 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-my-travel-journal-bookmarks-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journal-bookmarks/{userId}") {
            val targetUri = "/api/v1/travel-journal-bookmarks/{userId}"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                val response = createSliceTravelJournalBookmarkSummaryResponse()
                every {
                    travelJournalBookmarkService.getOtherTravelJournalBookmarks(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                    )
                } returns response
                it("200을 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_OTHER_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "get-other-travel-journal-bookmarks-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "여행일지 즐겨찾기 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID(travelJournalBookmarkId 값)" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example TravelJournalBookmarkSortType.values() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                    "content[].travelJournalBookmarkId" type JsonFieldType.NUMBER description "여행 일지 즐겨찾기 아이디" example response.content[0].travelJournalBookmarkId,
                                    "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                    "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                    "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                    "content[].travelJournalMainImageUrl" type JsonFieldType.STRING description "여행 일지의 대표 이미지 URL" example response.content[0].travelJournalMainImageUrl,
                                    "content[].isBookmarked" type JsonFieldType.BOOLEAN description "사용자가 즐겨찾기한 여행일지 여부" example response.content[0].isBookmarked,
                                    "content[].isRepresentative" type JsonFieldType.BOOLEAN description "여행 일지가 대표 여행일지인지 여부" example response.content[0].isRepresentative,
                                    "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                    "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                    "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                    "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                    "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                    "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                    "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                    "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("양수가 아닌 UserId가 들어온 경우") {
                it("400을 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-other-travel-journal-bookmarks-negative-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "양수가 아닌 USER ID" example TEST_INVALID_USER_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID(travelJournalBookmarkId 값)" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example TravelJournalBookmarkSortType.values() isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                it("401을 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_OTHER_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-other-travel-journal-bookmarks-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "userId" pathDescription "여행일지 즐겨찾기 목록을 조회할 USER ID" example TEST_USER_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID(travelJournalBookmarkId 값)" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example TravelJournalBookmarkSortType.values() isOptional true,
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/travel-journal-bookmarks/{travelJournalId}") {
            val targetUri = "/api/v1/travel-journal-bookmarks/{travelJournalId}"
            context("유효한 요청이 주어지는 경우") {
                every {
                    travelJournalBookmarkService.deleteTravelJournalBookmark(
                        TEST_USER_ID,
                        TEST_TRAVEL_JOURNAL_ID,
                    )
                } just runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "delete-travel-journal-bookmark",
                                pathParameters(
                                    "travelJournalId" pathDescription "여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 여행일지 아이디가 주어지는 경우") {
                every {
                    travelJournalBookmarkService.deleteTravelJournalBookmark(
                        TEST_USER_ID,
                        TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                    )
                } throws NoSuchElementException("$TEST_NOT_EXIST_TRAVEL_JOURNAL_ID: 존재하지 않는 커뮤니티입니다.")
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_NOT_EXIST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "delete-travel-journal-bookmark-not-exist",
                                pathParameters(
                                    "travelJournalId" pathDescription "존재하지 않는 여행일지 ID" example TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }

                context("유효하지 않은 토큰이 주어지는 경우") {
                    it("401을 반환한다.") {
                        restDocMockMvc.perform(
                            delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                        )
                            .andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "delete-travel-journal-bookmark-invalid-token",
                                    pathParameters(
                                        "travelJournalId" pathDescription "여행일지 ID" example TEST_TRAVEL_JOURNAL_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
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
