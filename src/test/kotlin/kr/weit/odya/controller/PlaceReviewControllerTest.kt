package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.placeReview.PlaceReviewSortType
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.PlaceReviewService
import kr.weit.odya.support.EXIST_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.FORBIDDEN_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SORT_TYPE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_EXIST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_HIGHEST_RATING
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_INVALID_USER_ID
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_OTHER_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_PLACE_REVIEW_COUNT
import kr.weit.odya.support.TEST_PLACE_REVIEW_ID
import kr.weit.odya.support.TEST_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_REVIEW
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_TOO_HIGH_RATING
import kr.weit.odya.support.TEST_TOO_LONG_REVIEW
import kr.weit.odya.support.TEST_TOO_LOW_RATING
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.creatSlicePlaceReviewResponse
import kr.weit.odya.support.createCountPlaceReviewResponse
import kr.weit.odya.support.createExistReviewResponse
import kr.weit.odya.support.createPlaceReviewRequest
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
import kr.weit.odya.support.updatePlaceReviewRequest
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(PlaceReviewController::class)
class PlaceReviewControllerTest(
    @MockkBean private val placeReviewService: PlaceReviewService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/place-reviews") {
            val targetUri = "/api/v1/place-reviews"
            context("유효한 요청 데이터가 전달되면") {
                val request = createPlaceReviewRequest()
                every { placeReviewService.createReview(request, TEST_USER_ID) } just Runs
                it("201를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 별점이 최소보다 미만 경우") {
                val request = createPlaceReviewRequest().copy(rating = TEST_TOO_LOW_RATING)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-too-low-rating",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "최소보다 미만인 별점" example TEST_TOO_LOW_RATING,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 별점이 최대보다 초과인 경우") {
                val request = createPlaceReviewRequest().copy(rating = TEST_TOO_HIGH_RATING)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-too-high-rating",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "최대보다 초과인 별점" example TEST_TOO_HIGH_RATING,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 리뷰가 공백인 경우") {
                val request = createPlaceReviewRequest().copy(review = " ")
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-blank-review",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING,
                                "review" type JsonFieldType.STRING description "공백인 리뷰" example " ",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 리뷰가 최대 길이를 초과한 경우") {
                val request = createPlaceReviewRequest().copy(review = TEST_TOO_LONG_REVIEW)
                it("400을 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-too-long-review",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING,
                                "review" type JsonFieldType.STRING description "최대 길이를 초과한 리뷰" example TEST_TOO_LONG_REVIEW,
                            ),
                        )
                    }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                val request = createPlaceReviewRequest()
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-not-registered-user",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 이미 리뷰한 장소를 리뷰한 경우") {
                val request = createPlaceReviewRequest()
                every { placeReviewService.createReview(request, TEST_USER_ID) } throws ExistResourceException(
                    EXIST_PLACE_REVIEW_ERROR_MESSAGE,
                )
                it("이미 리뷰를 작성한 장소입니다.라는 에러 메시지를 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-already-written-review",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "이미 리뷰한 장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = createPlaceReviewRequest()
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "placeReview-create-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW,
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/place-reviews") {
            val targetUri = "/api/v1/place-reviews"
            context("유효한 요청 데이터가 전달되면") {
                val request = updatePlaceReviewRequest()
                every { placeReviewService.updateReview(request, TEST_USER_ID) } just Runs
                it("204를 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 장소리뷰ID가 양수가 아닌 경우") {
                val request = updatePlaceReviewRequest().copy(id = TEST_INVALID_PLACE_REVIEW_ID)
                it("400을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-negative-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "양수가 아닌 장소 리뷰 ID" example TEST_INVALID_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 별점이 최소보다 미만 경우") {
                val request = updatePlaceReviewRequest().copy(rating = TEST_TOO_LOW_RATING)
                it("400을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-too-low-rating",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "최소보다 미만인 별점" example TEST_TOO_LOW_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 별점이 최대보다 초과인 경우") {
                val request = updatePlaceReviewRequest().copy(rating = TEST_TOO_HIGH_RATING)
                it("400을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-too-high-rating",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "최대보다 초과인 별점" example TEST_TOO_HIGH_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 리뷰가 공백인 경우") {
                val request = updatePlaceReviewRequest().copy(review = " ")
                it("400을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-too-short-review",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "공백인 리뷰" example " " isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 리뷰가 최대 길이를 초과한 경우") {
                val request = updatePlaceReviewRequest().copy(review = TEST_TOO_LONG_REVIEW)
                it("400을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-too-long-review",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "최대 길이를 초과한 리뷰" example TEST_TOO_LONG_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                val request = updatePlaceReviewRequest()
                it("401 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-not-registered-user",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_PLACE_REVIEW_ID isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_PLACE_REVIEW_ID isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 존재하지 않는 장소리뷰ID인 경우") {
                val request = updatePlaceReviewRequest().copy(id = TEST_EXIST_PLACE_REVIEW_ID)
                every { placeReviewService.updateReview(request, TEST_USER_ID) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("404를 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-not-found-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "잘못된 장소 리뷰 ID" example TEST_EXIST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 수정할 권한이 없는 경우") {
                val request = updatePlaceReviewRequest()
                every { placeReviewService.updateReview(request, TEST_USER_ID) } throws ForbiddenException(FORBIDDEN_PLACE_REVIEW_ERROR_MESSAGE)
                it("403을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isForbidden() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-no-permissions",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = updatePlaceReviewRequest()
                it("401 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "placeReview-update-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                "rating" type JsonFieldType.NUMBER description "별점" example TEST_HIGHEST_RATING isOptional true,
                                "review" type JsonFieldType.STRING description "리뷰" example TEST_REVIEW isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("DELETE /api/v1/place-reviews/{id}") {
            val targetUri = "/api/v1/place-reviews/{id}"
            context("유효한 요청 데이터가 전달되면") {
                every { placeReviewService.deleteReview(TEST_PLACE_REVIEW_ID, TEST_USER_ID) } just Runs
                it("204를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_PLACE_REVIEW_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "placeReview-delete-success",
                                pathParameters(
                                    "id" pathDescription "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 장소리뷰ID가 양수가 아닌 경우") {
                it("400를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INVALID_PLACE_REVIEW_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "placeReview-delete-fail-negative-id",
                                pathParameters(
                                    "id" pathDescription "양수가 아닌 장소 리뷰 ID" example TEST_INVALID_PLACE_REVIEW_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_PLACE_REVIEW_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-delete-fail-not-registered-user",
                                pathParameters(
                                    "id" pathDescription "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 존재하지 않는 장소리뷰ID인 경우") {
                every { placeReviewService.deleteReview(TEST_EXIST_PLACE_REVIEW_ID, TEST_USER_ID) } throws NoSuchElementException(NOT_EXIST_PLACE_REVIEW_ERROR_MESSAGE)
                it("404를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_EXIST_PLACE_REVIEW_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "placeReview-delete-not-found-id",
                                pathParameters(
                                    "id" pathDescription "존재하지 않는 장소 리뷰 ID" example TEST_EXIST_PLACE_REVIEW_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만, 삭제할 권한이 없는 경우") {
                every { placeReviewService.deleteReview(TEST_PLACE_REVIEW_ID, TEST_USER_ID) } throws ForbiddenException(FORBIDDEN_PLACE_REVIEW_ERROR_MESSAGE)
                it("403를 반환한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_PLACE_REVIEW_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "placeReview-delete-no-permissions",
                                pathParameters(
                                    "id" pathDescription "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
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
                            .delete(targetUri, TEST_PLACE_REVIEW_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-delete-invalid-token",
                                pathParameters(
                                    "id" pathDescription "장소 리뷰 ID" example TEST_PLACE_REVIEW_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/place-reviews/places/{id}") {
            val targetUri = "/api/v1/place-reviews/places/{id}"
            context("유효한 요청 데이터가 전달되면") {
                val response = creatSlicePlaceReviewResponse()
                val content = response.content[0]
                every { placeReviewService.getByPlaceReviewList(TEST_PLACE_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) } returns response
                it("200 및 장소 리스트를 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "placeReview-placeId-get-list-success",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                    "averageRating" type JsonFieldType.NUMBER description "장소 평균 별점" example response.averageRating,
                                    "content[].id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example content.id,
                                    "content[].placeId" type JsonFieldType.STRING description "장소 ID" example content.placeId,
                                    "content[].userId" type JsonFieldType.NUMBER description "유저 ID" example content.userId,
                                    "content[].writerNickname" type JsonFieldType.STRING description "유저 닉네임" example content.writerNickname,
                                    "content[].starRating" type JsonFieldType.NUMBER description "별점" example content.starRating,
                                    "content[].review" type JsonFieldType.STRING description "리뷰" example content.review,
                                    "content[].createdAt" type JsonFieldType.STRING description "생성일시" example content.createdAt,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 조회할 마지막 ID가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "placeReview-placeId-get-fail-invalid-lastId",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 size가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "placeReview-placeId-get-fail-invalid-size",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-placeId-get-fail-not-registered-user",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-placeId-get-fail-invalid-token",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/place-reviews/users/{id}") {
            val targetUri = "/api/v1/place-reviews/users/{id}"
            context("유효한 요청 데이터가 전달되면") {
                val response = creatSlicePlaceReviewResponse()
                val content = response.content[0]
                every { placeReviewService.getByUserReviewList(TEST_USER_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-list-success",
                                pathParameters(
                                    "id" pathDescription "유저 ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                    "averageRating" type JsonFieldType.NUMBER description "장소 평균 별점" example response.averageRating,
                                    "content[].id" type JsonFieldType.NUMBER description "장소 리뷰 ID" example content.id,
                                    "content[].placeId" type JsonFieldType.STRING description "장소 ID" example content.placeId,
                                    "content[].userId" type JsonFieldType.NUMBER description "유저 ID" example content.userId,
                                    "content[].writerNickname" type JsonFieldType.STRING description "유저 닉네임" example content.writerNickname,
                                    "content[].starRating" type JsonFieldType.NUMBER description "별점" example content.starRating,
                                    "content[].review" type JsonFieldType.STRING description "리뷰" example content.review,
                                    "content[].createdAt" type JsonFieldType.STRING description "생성일시" example content.createdAt,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 유저 ID가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-fail-negative-id",
                                pathParameters(
                                    "id" pathDescription "양수가 아닌 유저 ID" example TEST_INVALID_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
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
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-fail-invalid-lastId",
                                pathParameters(
                                    "id" pathDescription "유저 ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 조회할 size가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-fail-invalid-size",
                                pathParameters(
                                    "id" pathDescription "유저 ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("가입되어 있지 않은 USERID이 주어지는 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param("sortType", TEST_PLACE_SORT_TYPE.name),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-fail-not-registered-user",
                                pathParameters(
                                    "id" pathDescription "유저 ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 가입되지 않은 USER ID로 조회하려는 경우") {
                every { placeReviewService.getByUserReviewList(TEST_NOT_EXIST_USER_ID, TEST_SIZE, TEST_PLACE_SORT_TYPE, TEST_LAST_ID) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_NOT_EXIST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-fail-lookup-not-registered-id",
                                pathParameters(
                                    "id" pathDescription "가입되어 있지 않은 유저 ID" example TEST_NOT_EXIST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
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
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(SORT_TYPE_PARAM, TEST_PLACE_SORT_TYPE.name)
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-userId-get-fail-invalid-token",
                                pathParameters(
                                    "id" pathDescription "유저 ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_SIZE isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 기준 (default = LATEST)" example PlaceReviewSortType.values()
                                        .joinToString() isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 데이터의 ID" example TEST_LAST_ID isOptional true,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/place-reviews/{id}") {
            val targetUri = "/api/v1/place-reviews/{id}"
            context("이미 한줄 리뷰를 작성한 장소의 유효한 데이터가 전달되면") {
                every { placeReviewService.getExistReview(TEST_USER_ID, TEST_PLACE_ID) } returns createExistReviewResponse()
                it("200 및 true 반환") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "placeReview-exist-get-true-success",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "exist" type JsonFieldType.BOOLEAN description "이미 작성한 리뷰가 있는지 여부" example true,
                                ),
                            ),
                        )
                }
            }

            context("한줄 리뷰를 작성하지 않은 장소의 유효한 데이터가 전달되면") {
                every { placeReviewService.getExistReview(TEST_USER_ID, TEST_OTHER_PLACE_ID) } returns createExistReviewResponse(false)
                it("200 및 false 반환") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_OTHER_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "placeReview-exist-get-false-success",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_OTHER_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "exist" type JsonFieldType.BOOLEAN description "이미 작성한 리뷰가 있는지 여부" example false,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 반환") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-exist-get-fail-invalid-token",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/place-reviews/count/{id}") {
            val targetUri = "/api/v1/place-reviews/count/{id}"
            context("유효한 토큰과 placeId가 전달되면") {
                every { placeReviewService.getReviewCount(TEST_PLACE_ID) } returns createCountPlaceReviewResponse()
                it("200 및 해당 장소의 한줄 리뷰 수 반환") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "placeReview-count-get-success",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "count" type JsonFieldType.NUMBER description "해당 장소의 리뷰 수" example TEST_PLACE_REVIEW_COUNT,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 반환") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_PLACE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "placeReview-count-get-fail-invalid-token",
                                pathParameters(
                                    "id" pathDescription "장소 ID" example TEST_PLACE_ID,
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
