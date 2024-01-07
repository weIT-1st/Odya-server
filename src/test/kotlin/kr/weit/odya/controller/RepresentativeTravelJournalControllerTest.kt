package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalSortType
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.RepresentativeTravelJournalService
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SORT_TYPE_PARAM
import kr.weit.odya.support.TEST_ALREADY_REGISTERED_REP_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_OTHER_REP_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_OTHER_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_REP_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_REP_TRAVEL_JOURNAL_SORT_TYPE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createOtherSimpleUserResponse
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createRepTravelJournalSummaryResponse
import kr.weit.odya.support.createSliceRepTravelJournalSummaryResponse
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
@WebMvcTest(RepresentativeTravelJournalController::class)
class RepresentativeTravelJournalControllerTest(
    @MockkBean private val repTravelJournalService: RepresentativeTravelJournalService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/rep-travel-journals/{travelJournalId}") {
            val targetUri = "/api/v1/rep-travel-journals/{travelJournalId}"
            context("유효한 요청이 주어지는 경우") {
                every {
                    repTravelJournalService.createRepTravelJournal(
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
                                "create-rep-travel-journal",
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

            context("자신의 여행일지가 아닌 경우") {
                every {
                    repTravelJournalService.createRepTravelJournal(
                        TEST_USER_ID,
                        TEST_OTHER_TRAVEL_JOURNAL_ID,
                    )
                } throws ForbiddenException("대표 여행일지로 등록할 수 있는 여행일지($TEST_OTHER_TRAVEL_JOURNAL_ID)는 자신의 것만 가능합니다.")
                it("403를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_OTHER_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "create-rep-travel-journal-not-my-travel-journal",
                                pathParameters(
                                    "travelJournalId" pathDescription "자신의 여행일지가 아닌 여행일지 아이디" example TEST_OTHER_TRAVEL_JOURNAL_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("이미 대표 여행일지로 설정한 여행일지인 경우") {
                every {
                    repTravelJournalService.createRepTravelJournal(
                        TEST_USER_ID,
                        TEST_ALREADY_REGISTERED_REP_TRAVEL_JOURNAL_ID,
                    )
                } throws ExistResourceException("이미 사용자($TEST_USER_ID)가 대표 여행일지($TEST_ALREADY_REGISTERED_REP_TRAVEL_JOURNAL_ID)로 등록하였습니다.")
                it("409를 반환한다.") {
                    restDocMockMvc.perform(
                        post(targetUri, TEST_ALREADY_REGISTERED_REP_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isConflict)
                        .andDo(
                            createPathDocument(
                                "create-rep-travel-journal-already-registered",
                                pathParameters(
                                    "travelJournalId" pathDescription "이미 대표 여행일지로 설정한 여행일지 ID" example TEST_ALREADY_REGISTERED_REP_TRAVEL_JOURNAL_ID,
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
                                "create-rep-travel-journal-invalid-token",
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

        describe("GET /api/v1/rep-travel-journals/me") {
            val targetUri = "/api/v1/rep-travel-journals/me"
            context("유효한 요청이 주어지는 경우") {
                val response = createSliceRepTravelJournalSummaryResponse()
                every {
                    repTravelJournalService.getMyRepTravelJournals(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_REP_TRAVEL_JOURNAL_SORT_TYPE,
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-my-rep-travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID(repTravelJournalId 값)" example "null" isOptional true,
                                SORT_TYPE_PARAM parameterDescription "정렬 타입" example RepresentativeTravelJournalSortType.values() isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].repTravelJournalId" type JsonFieldType.NUMBER description "대표 여행 일지 아이디" example response.content[0].repTravelJournalId,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.content[0].travelEndDate,
                                "content[].travelJournalMainImageUrl" type JsonFieldType.STRING description "여행 일지의 대표 이미지 URL" example response.content[0].travelJournalMainImageUrl,
                                "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                "content[].travelCompanionSimpleResponses[].username" type JsonFieldType.STRING description "여행 동행자의 이름" example response.content[0].travelCompanionSimpleResponses?.get(
                                    0,
                                )?.username isOptional true,
                                "content[].travelCompanionSimpleResponses[].profileUrl" type JsonFieldType.STRING description "여행 동행자의 프로필 사진" example response.content[0].travelCompanionSimpleResponses?.get(
                                    0,
                                )?.profileUrl isOptional true,
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
                            "get-my-rep-travel-journals-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID(repTravelJournalId 값)" example "null" isOptional true,
                                SORT_TYPE_PARAM parameterDescription "정렬 타입" example RepresentativeTravelJournalSortType.values() isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/rep-travel-journals/{targetUserId}") {
            val targetUri = "/api/v1/rep-travel-journals/{targetUserId}"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                val response = createSliceRepTravelJournalSummaryResponse(
                    repTravelJournalSummaryResponses = listOf(
                        createRepTravelJournalSummaryResponse(
                            userSimpleResponse = createOtherSimpleUserResponse(
                                user = createOtherUser(),
                            ),
                        ),
                    ),
                )
                every {
                    repTravelJournalService.getTargetRepTravelJournals(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_REP_TRAVEL_JOURNAL_SORT_TYPE,
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
                                "get-other-rep-travel-journals-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "targetUserId" pathDescription "대표 여행일지를 조회할 사용자 ID" example TEST_OTHER_USER_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID(repTravelJournalId 값)" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example RepresentativeTravelJournalSortType.values() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                    "content[].repTravelJournalId" type JsonFieldType.NUMBER description "대표 여행 일지 아이디" example response.content[0].repTravelJournalId,
                                    "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                    "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                    "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                    "content[].travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.content[0].travelEndDate,
                                    "content[].travelJournalMainImageUrl" type JsonFieldType.STRING description "여행 일지의 대표 이미지 URL" example response.content[0].travelJournalMainImageUrl,
                                    "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                    "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                    "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                    "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                    "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                    "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                    "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                    "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                    "content[].travelCompanionSimpleResponses[].username" type JsonFieldType.STRING description "여행 동행자의 이름" example response.content[0].travelCompanionSimpleResponses?.get(
                                        0,
                                    )?.username isOptional true,
                                    "content[].travelCompanionSimpleResponses[].profileUrl" type JsonFieldType.STRING description "여행 동행자의 프로필 사진" example response.content[0].travelCompanionSimpleResponses?.get(
                                        0,
                                    )?.profileUrl isOptional true,
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
                                "get-other-rep-travel-journals-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "targetUserId" pathDescription "대표 여행일지를 조회할 사용자 ID" example TEST_OTHER_USER_ID,
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID(repTravelJournalId 값)" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example RepresentativeTravelJournalSortType.values() isOptional true,
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/rep-travel-journals/{repTravelJournalId}") {
            val targetUri = "/api/v1/rep-travel-journals/{repTravelJournalId}"
            context("유효한 요청이 주어지는 경우") {
                every {
                    repTravelJournalService.deleteRepTravelJournal(
                        TEST_USER_ID,
                        TEST_REP_TRAVEL_JOURNAL_ID,
                    )
                } just runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_REP_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "delete-rep-travel-journal-success",
                                pathParameters(
                                    "repTravelJournalId" pathDescription "대표 여행일지 ID" example TEST_REP_TRAVEL_JOURNAL_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("대표 여행일지 작성자와 다른 경우") {
                every {
                    repTravelJournalService.deleteRepTravelJournal(
                        TEST_USER_ID,
                        TEST_OTHER_REP_TRAVEL_JOURNAL_ID,
                    )
                } throws ForbiddenException("대표 여행일지($TEST_OTHER_REP_TRAVEL_JOURNAL_ID)는 자신의 것만 삭제할 수 있습니다.")
                it("403를 반환한다.") {
                    restDocMockMvc.perform(
                        delete(targetUri, TEST_OTHER_REP_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "delete-rep-travel-journal-not-my-rep-travel-journal",
                                pathParameters(
                                    "repTravelJournalId" pathDescription "작성자가 다른 대표 여행일지 ID" example TEST_OTHER_REP_TRAVEL_JOURNAL_ID,
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
                            delete(targetUri, TEST_REP_TRAVEL_JOURNAL_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                        )
                            .andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "delete-rep-travel-journal-invalid-token",
                                    pathParameters(
                                        "repTravelJournalId" pathDescription "대표 여행일지 ID" example TEST_REP_TRAVEL_JOURNAL_ID,
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
