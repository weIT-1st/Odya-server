package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.domain.report.ReportReason
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.ReportService
import kr.weit.odya.support.ALREADY_REPORT_POST
import kr.weit.odya.support.CAN_NOT_REPORT_OWN_POST
import kr.weit.odya.support.NOT_EXIST_COMMUNITY_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_TRAVEL_JOURNAL_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_INVALID_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_NOT_EXIST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_NOT_EXIST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_TOO_LONG_PHRASE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createReportCommunityRequest
import kr.weit.odya.support.createReportPlaceReviewRequest
import kr.weit.odya.support.createReportTravelJournalRequest
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(ReportController::class)
class ReportControllerTest(
    @MockkBean
    private val reportService: ReportService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/reports/place-review") {
            val targetUri = "/api/v1/reports/place-review"
            val request = createReportPlaceReviewRequest()
            context("유효한 유저와 등록된 신고 사유가 전달되면") {
                every { reportService.reportPlaceReview(TEST_USER_ID, any()) } just Runs
                it("신고 등록 및 신고 횟수 따른 삭제 후 204을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "report-place-review-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "한 줄 리뷰 ID" example request.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 유저와 기타 사유가 전달되면") {
                val otherRequest = createReportPlaceReviewRequest().copy(reportReason = ReportReason.OTHER, otherReason = "기타 사유")
                every { reportService.reportPlaceReview(TEST_USER_ID, any()) } just runs
                it("기타 신고 사유 등록 및 신고 횟수에 따른 삭제 후 204을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "report-place-review-other-reason-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "한 줄 리뷰 ID" example otherRequest.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("양수가 아닌 한 줄 리뷰ID가 전달되면") {
                val otherRequest = createReportPlaceReviewRequest().copy(placeReviewId = TEST_INVALID_PLACE_REVIEW_ID)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-invalid-place-review-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "양수가 아닌 한 줄 리뷰 ID" example otherRequest.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("공백인 기타 신고 사유가 전달되면") {
                val otherRequest = createReportPlaceReviewRequest().copy(otherReason = "")
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-empty-other-reason",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "한 줄 리뷰 ID" example otherRequest.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "공백인 기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("20자를 초과한 기타 신고 사유가 전달되면") {
                val otherRequest = createReportPlaceReviewRequest().copy(otherReason = TEST_TOO_LONG_PHRASE)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-too-long-other-reason",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "한 줄 리뷰 ID" example otherRequest.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "20자를 초과한 기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("존재하지 않는 한 줄 리뷰ID가 전달되면") {
                val otherRequest = request.copy(placeReviewId = TEST_NOT_EXIST_PLACE_REVIEW_ID)
                every { reportService.reportPlaceReview(TEST_USER_ID, any()) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("404을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-not-exist-place-review-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "존재하지 않는 한 줄 리뷰 ID" example otherRequest.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("작성자가 자신의 글을 신고 등록 요청 시") {
                every { reportService.reportPlaceReview(TEST_USER_ID, any()) } throws IllegalArgumentException(CAN_NOT_REPORT_OWN_POST)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-can-not-report-own-post",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "신고한 유저가 쓴 한 줄 리뷰 ID" example request.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("이미 신고한 한 줄 리뷰인 경우") {
                every { reportService.reportPlaceReview(TEST_USER_ID, any()) } throws ExistResourceException(ALREADY_REPORT_POST)
                it("409을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-already-report-post",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "이미 신고한 한 줄 리뷰 ID" example request.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "report-place-review-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "placeReviewId" type JsonFieldType.NUMBER description "한 줄 리뷰 ID" example request.placeReviewId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/reports/travel-journal") {
            val targetUri = "/api/v1/reports/travel-journal"
            val request = createReportTravelJournalRequest()
            context("유효한 유저와 등록된 신고 사유가 전달되면") {
                every { reportService.reportTravelJournal(TEST_USER_ID, any()) } just Runs
                it("신고 등록 및 신고 횟수 따른 삭제 후 204을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "여행 일지 ID" example request.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 유저와 기타 신고 사유가 전달되면") {
                val otherRequest = createReportTravelJournalRequest().copy(reportReason = ReportReason.OTHER, otherReason = "기타 사유")
                every { reportService.reportTravelJournal(TEST_USER_ID, any()) } just Runs
                it("기타 신고 사유 등록 및 신고 횟수 따른 삭제 후 204을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-other-reason-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "여행 일지 ID" example otherRequest.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("양수가 아닌 여행 일지ID가 전달되면") {
                val otherRequest = createReportTravelJournalRequest().copy(travelJournalId = TEST_INVALID_TRAVEL_JOURNAL_ID)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-invalid-travel-journal-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "양수가 아닌 여행 일지 ID" example otherRequest.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("공백인 기타 신고 사유가 전달되면") {
                val otherRequest = createReportTravelJournalRequest().copy(otherReason = "")
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-empty-other-reason",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "여행 일지 ID" example otherRequest.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "공백인 기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("20자를 초과한 기타 신고 사유가 전달되면") {
                val otherRequest = createReportTravelJournalRequest().copy(otherReason = TEST_TOO_LONG_PHRASE)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-too-long-other-reason",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "여행 일지 ID" example otherRequest.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "20자를 초과한 기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("존재하지 않는 여행 일지ID가 전달되면") {
                val otherRequest = request.copy(travelJournalId = TEST_NOT_EXIST_TRAVEL_JOURNAL_ID)
                every { reportService.reportTravelJournal(TEST_USER_ID, any()) } throws NoSuchElementException(NOT_EXIST_TRAVEL_JOURNAL_ERROR_MESSAGE)
                it("404을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-not-exist-travel-journal-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "존재하지 않는 여행 일지 ID" example otherRequest.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("작성자가 자신의 글을 신고 등록 요청 시") {
                every { reportService.reportTravelJournal(TEST_USER_ID, any()) } throws IllegalArgumentException(CAN_NOT_REPORT_OWN_POST)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-can-not-report-own-post",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "신고한 유저가 쓴 여행 일지 ID" example request.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("이미 신고한 여행 일지인 경우") {
                every { reportService.reportTravelJournal(TEST_USER_ID, any()) } throws ExistResourceException(ALREADY_REPORT_POST)
                it("409을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-already-report-post",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "이미 신고한 여행 일지 ID" example request.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "report-travel-journal-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "travelJournalId" type JsonFieldType.NUMBER description "여행 일지 ID" example request.travelJournalId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/reports/community") {
            val targetUri = "/api/v1/reports/community"
            val request = createReportCommunityRequest()
            context("유효한 유저와 등록된 신고 사유가 전달되면") {
                every { reportService.reportCommunity(TEST_USER_ID, any()) } just Runs
                it("신고 등록 및 신고 횟수 따른 삭제 후 204을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "report-community-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "커뮤니티 ID" example request.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 유저와 기타 신고 사유가 전달되면") {
                val otherRequest = createReportCommunityRequest().copy(reportReason = ReportReason.OTHER, otherReason = "기타 사유")
                every { reportService.reportCommunity(TEST_USER_ID, any()) } just Runs
                it("기타 신고 사유 등록 및 신고 횟수 따른 삭제 후 204을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "report-community-other-reason-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "커뮤니티 ID" example otherRequest.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("양수가 아닌 커뮤니티ID가 전달되면") {
                val otherRequest = createReportCommunityRequest().copy(communityId = TEST_INVALID_TRAVEL_JOURNAL_ID)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-invalid-community-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "양수가 아닌 커뮤니티 ID" example otherRequest.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("공백인 기타 신고 사유가 전달되면") {
                val otherRequest = createReportCommunityRequest().copy(otherReason = "")
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-empty-other-reason",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "커뮤니티 ID" example otherRequest.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "공백인 기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("20자를 초과한 기타 신고 사유가 전달되면") {
                val otherRequest = createReportCommunityRequest().copy(otherReason = TEST_TOO_LONG_PHRASE)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-too-long-other-reason",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "커뮤니티 ID" example otherRequest.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "20자를 초과한 기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("존재하지 않는 커뮤니티ID가 전달되면") {
                val otherRequest = request.copy(communityId = TEST_NOT_EXIST_TRAVEL_JOURNAL_ID)
                every { reportService.reportCommunity(TEST_USER_ID, any()) } throws NoSuchElementException(NOT_EXIST_COMMUNITY_ERROR_MESSAGE)
                it("404을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(otherRequest)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-not-exist-community-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "존재하지 않는 커뮤니티 ID" example otherRequest.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example otherRequest.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example otherRequest.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("작성자가 자신의 글을 신고 등록 요청 시") {
                every { reportService.reportCommunity(TEST_USER_ID, any()) } throws IllegalArgumentException(CAN_NOT_REPORT_OWN_POST)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-can-not-report-own-post",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "신고한 유저가 쓴 커뮤니티 ID" example request.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("이미 신고한 커뮤니티인 경우") {
                every { reportService.reportCommunity(TEST_USER_ID, any()) } throws ExistResourceException(ALREADY_REPORT_POST)
                it("409을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-already-report-post",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "이미 신고한 커뮤니티 ID" example request.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "report-community-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "communityId" type JsonFieldType.NUMBER description "커뮤니티 ID" example request.communityId,
                                "reportReason" type JsonFieldType.STRING description "신고 사유" example request.reportReason,
                                "otherReason" type JsonFieldType.STRING description "기타 사유" example request.otherReason isOptional true,
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
