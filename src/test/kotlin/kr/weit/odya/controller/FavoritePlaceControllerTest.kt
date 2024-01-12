package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.FavoritePlaceService
import kr.weit.odya.support.EXIST_FAVORITE_PLACE_ERROR_MESSAGE
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.NOT_FOUND_FAVORITE_PLACE_ERROR_MESSAGE
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SORT_TYPE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_EXIST_FAVORITE_PLACE_ID
import kr.weit.odya.support.TEST_FAVORITE_PLACE_COUNT
import kr.weit.odya.support.TEST_FAVORITE_PLACE_ID
import kr.weit.odya.support.TEST_FAVORITE_PLACE_INVALID_SORT_TYPE
import kr.weit.odya.support.TEST_FAVORITE_PLACE_SORT_TYPE
import kr.weit.odya.support.TEST_INVALID_FAVORITE_PLACE_ID
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_INVALID_USER_ID
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFavoritePlaceRequest
import kr.weit.odya.support.createSliceFavoritePlaceResponse
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(FavoritePlaceController::class)
class FavoritePlaceControllerTest(
    @MockkBean private val favoritePlaceService: FavoritePlaceService,
    private val context: WebApplicationContext,
) : DescribeSpec(
        {
            val restDocumentation = ManualRestDocumentation()
            val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

            beforeEach {
                restDocumentation.beforeTest(javaClass, it.name.testName)
            }

            describe("POST /api/v1/favorite-places") {
                val targetUri = "/api/v1/favorite-places"
                context("유효한 요청 데이터가 전달되면") {
                    val request = createFavoritePlaceRequest()
                    every { favoritePlaceService.createFavoritePlace(TEST_USER_ID, createFavoritePlaceRequest()) } just Runs
                    it("201를 반환한다.") {
                        restDocMockMvc.post(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            jsonContent(request)
                        }.andExpect {
                            status { isCreated() }
                        }.andDo {
                            createDocument(
                                "favorite-place-create-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                requestBody(
                                    "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                ),
                            )
                        }
                    }
                }

                context("유효한 토큰이면서 장소 ID가 빈 문자열이면") {
                    val request = createFavoritePlaceRequest().copy(placeId = " ")
                    it("400를 반환한다.") {
                        restDocMockMvc.post(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            jsonContent(request)
                        }.andExpect {
                            status { isBadRequest() }
                        }.andDo {
                            createDocument(
                                "favorite-place-create-failed-empty-place-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                requestBody(
                                    "placeId" type JsonFieldType.STRING description "공백인 장소 ID" example """ """,
                                ),
                            )
                        }
                    }
                }

                context("가입되어 있지 않은 USERID이 주어지는 경우") {
                    val request = createFavoritePlaceRequest()
                    it("401를 반환한다.") {
                        restDocMockMvc.post(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                            jsonContent(request)
                        }.andExpect {
                            status { isUnauthorized() }
                        }.andDo {
                            createDocument(
                                "favorite-place-create-failed-not-exist-user-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                requestBody(
                                    "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                ),
                            )
                        }
                    }
                }

                context("유효한 토큰이지만, 이미 관심 장소인 경우") {
                    val request = createFavoritePlaceRequest()
                    every {
                        favoritePlaceService.createFavoritePlace(TEST_USER_ID, createFavoritePlaceRequest())
                    } throws ExistResourceException(EXIST_FAVORITE_PLACE_ERROR_MESSAGE)
                    it("409를 반환한다.") {
                        restDocMockMvc.post(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            jsonContent(request)
                        }.andExpect {
                            status { isConflict() }
                        }.andDo {
                            createDocument(
                                "favorite-place-create-failed-already-exist-favorite-place",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                requestBody(
                                    "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                ),
                            )
                        }
                    }
                }

                context("유효하지 않은 토큰이 전달되면") {
                    val request = createFavoritePlaceRequest()
                    it("401를 반환한다.") {
                        restDocMockMvc.post(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            jsonContent(request)
                        }.andExpect {
                            status { isUnauthorized() }
                        }.andDo {
                            createDocument(
                                "favorite-place-create-failed-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                requestBody(
                                    "placeId" type JsonFieldType.STRING description "장소 ID" example TEST_PLACE_ID,
                                ),
                            )
                        }
                    }
                }
            }

            describe("DELETE /api/v1/favorite-places/{id}") {
                val targetUri = "/api/v1/favorite-places/{id}"
                context("유효한 요청 데이터가 전달되면") {
                    every { favoritePlaceService.deleteFavoritePlace(TEST_USER_ID, TEST_FAVORITE_PLACE_ID) } just Runs
                    it("204를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .delete(targetUri, TEST_FAVORITE_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        ).andExpect(status().isNoContent)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-delete-success",
                                    pathParameters(
                                        "id" pathDescription "관심 장소 ID" example TEST_FAVORITE_PLACE_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }

                context("양수가 아닌 관심 장소 ID인 경우") {
                    it("400를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .delete(targetUri, TEST_INVALID_FAVORITE_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-delete-failed-invalid-favorite-place-id",
                                    pathParameters(
                                        "id" pathDescription "양수가 아닌 관심 장소 ID" example TEST_INVALID_FAVORITE_PLACE_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }

                context("가입되어 있지 않은 USERID이 주어지는 경우") {
                    it("401를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .delete(targetUri, TEST_FAVORITE_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                        ).andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-delete-failed-not-exist-user-id",
                                    pathParameters(
                                        "id" pathDescription "관심 장소 ID" example TEST_FAVORITE_PLACE_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 토큰이지만, 관심 장소가 아닌 경우") {
                    every {
                        favoritePlaceService.deleteFavoritePlace(TEST_USER_ID, TEST_EXIST_FAVORITE_PLACE_ID)
                    } throws NoSuchElementException(NOT_FOUND_FAVORITE_PLACE_ERROR_MESSAGE)
                    it("404를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .delete(targetUri, TEST_EXIST_FAVORITE_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        ).andExpect(status().isNotFound)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-delete-failed-not-exist-favorite-place-id",
                                    pathParameters(
                                        "id" pathDescription "존재하지 않는 관심 장소 ID" example TEST_EXIST_FAVORITE_PLACE_ID,
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
                                .delete(targetUri, TEST_FAVORITE_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                        ).andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-delete-failed-invalid-token",
                                    pathParameters(
                                        "id" pathDescription "관심 장소 ID" example TEST_FAVORITE_PLACE_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }
            }

            describe("GET /api/v1/favorite-places/{placeId}") {
                val targetUri = "/api/v1/favorite-places/{placeId}"
                context("유효한 요청 데이터가 전달되면") {
                    every { favoritePlaceService.getFavoritePlace(TEST_USER_ID, TEST_PLACE_ID) } returns true
                    it("200를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        ).andExpect(status().isOk)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-check-success",
                                    pathParameters(
                                        "placeId" pathDescription "장소 ID" example TEST_PLACE_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }

                context("placeId가 공백인 경우") {
                    it("400를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, " ")
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-check-failed-empty-place-id",
                                    pathParameters(
                                        "placeId" pathDescription "공백인 장소 ID" example " ",
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }

                context("유효하지 않은 토큰 전달되면") {
                    it("401를 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_PLACE_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                        ).andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-check-failed-invalid-token",
                                    pathParameters(
                                        "placeId" pathDescription "장소 ID" example TEST_PLACE_ID,
                                    ),
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
                                ),
                            )
                    }
                }
            }

            describe("GET /api/v1/favorite-places/counts") {
                val targetUri = "/api/v1/favorite-places/counts"
                context("유효한 USERID가 전달되면") {
                    every { favoritePlaceService.getFavoritePlaceCount(TEST_USER_ID) } returns TEST_FAVORITE_PLACE_COUNT
                    it("관심장소 수를 출력한다.") {
                        restDocMockMvc.get(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        }.andExpect {
                            status { isOk() }
                        }.andDo {
                            createDocument(
                                "favorite-place-count-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            )
                        }
                    }
                }

                context("유효하지않은 토큰이 전달되면") {
                    it("401을 출력한다.") {
                        restDocMockMvc.get(targetUri) {
                            header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        }.andExpect {
                            status { isUnauthorized() }
                        }.andDo {
                            createDocument(
                                "favorite-place-count-failed-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            )
                        }
                    }
                }
            }

            describe("GET /api/v1/favorite-places/counts/{userId}") {
                val targetUri = "/api/v1/favorite-places/counts/{userId}"
                context("유효한 USERID가 전달되면") {
                    every { favoritePlaceService.getFavoritePlaceCount(TEST_OTHER_USER_ID) } returns TEST_FAVORITE_PLACE_COUNT
                    it("관심장소 수를 출력한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_OTHER_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        )
                            .andExpect(status().isOk)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-count-success",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_OTHER_USER_ID,
                                    ),
                                ),
                            )
                    }
                }

                context("양수가 아닌 USERID가 전달되면") {
                    it("400을 출력한다") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_INVALID_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        )
                            .andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-count-failed-invalid-favorite-place-id",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "양수가 아닌 USER ID" example TEST_INVALID_USER_ID,
                                    ),
                                ),
                            )
                    }
                }

                context("유효하지않은 토큰이 전달되면") {
                    it("401을 출력한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_OTHER_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                        )
                            .andExpect(status().isUnauthorized())
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-count-failed-invalid-token",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_OTHER_USER_ID,
                                    ),
                                ),
                            )
                    }
                }
            }

            describe("GET /api/v1/favorite-places/list") {
                val targetUri = "/api/v1/favorite-places/list"
                context("유효한 USERID와 size,sortType,lastId가 전달되면") {
                    val response = createSliceFavoritePlaceResponse()
                    val content = response.content[0]
                    every {
                        favoritePlaceService.getMyFavoritePlaceList(TEST_USER_ID, TEST_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, TEST_LAST_ID)
                    } returns createSliceFavoritePlaceResponse()
                    it("관심장소 수를 출력한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(SIZE_PARAM, TEST_SIZE.toString())
                                .param(SORT_TYPE_PARAM, TEST_FAVORITE_PLACE_SORT_TYPE.name)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isOk)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-list-success",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                    responseBody(
                                        "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                        "content[].id" type JsonFieldType.NUMBER description "관심 장소 ID" example content.id,
                                        "content[].placeId" type JsonFieldType.STRING description "장소 ID" example content.placeId,
                                        "content[].userId" type JsonFieldType.NUMBER description "유저 ID" example content.userId,
                                        "content[].isFavoritePlace" type JsonFieldType.BOOLEAN description "관심 장소 여부" example content.isFavoritePlace,
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 USERID만 전달되면") {
                    val response = createSliceFavoritePlaceResponse()
                    val content = response.content[0]
                    every {
                        favoritePlaceService.getMyFavoritePlaceList(TEST_USER_ID, TEST_DEFAULT_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, null)
                    } returns createSliceFavoritePlaceResponse()
                    it("관심장소 수를 출력한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                        ).andExpect(status().isOk)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-list-request-param-null-success",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example "null" isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example "null" isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example "null" isOptional true,
                                    ),
                                    responseBody(
                                        "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                        "content[].id" type JsonFieldType.NUMBER description "관심 장소 ID" example content.id,
                                        "content[].placeId" type JsonFieldType.STRING description "장소 ID" example content.placeId,
                                        "content[].userId" type JsonFieldType.NUMBER description "유저 ID" example content.userId,
                                        "content[].isFavoritePlace" type JsonFieldType.BOOLEAN description "관심 장소 여부" example content.isFavoritePlace,
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 USERID와 양수가 아닌 size가 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                                .param(SORT_TYPE_PARAM, TEST_FAVORITE_PLACE_SORT_TYPE.name)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-list-failed-invalid-size",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "양수가 아닌 츨력할 리스트 사이즈(default=10)" example TEST_INVALID_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 USERID와 정의하지 않은 정렬 기준이 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(SIZE_PARAM, TEST_SIZE.toString())
                                .param(SORT_TYPE_PARAM, TEST_FAVORITE_PLACE_INVALID_SORT_TYPE)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-list-failed-invalid-sort-type",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_INVALID_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "정의되지 않은 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_INVALID_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 USERID와 양수가 아닌 마지막 ID가 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(SIZE_PARAM, TEST_SIZE.toString())
                                .param(SORT_TYPE_PARAM, TEST_FAVORITE_PLACE_SORT_TYPE.name)
                                .param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString()),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-list-failed-invalid-last-id",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "양수가 아닌 마지막 리스트 ID" example TEST_INVALID_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("유효하지 않은 토큰이 전달되면") {
                    it("401을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                                .param(SIZE_PARAM, TEST_SIZE.toString())
                                .param(SORT_TYPE_PARAM, TEST_FAVORITE_PLACE_SORT_TYPE.name)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "favorite-place-list-failed-invalid-token",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }
            }

            describe("GET /api/v1/favorite-places/list/{userId}") {
                val targetUri = "/api/v1/favorite-places/list/{userId}"
                context("유효한 USERID와 size,sortType,lastId가 전달되면") {
                    val response = createSliceFavoritePlaceResponse()
                    val content = response.content[0]
                    every {
                        favoritePlaceService.getFavoritePlaceList(TEST_USER_ID, TEST_USER_ID, TEST_DEFAULT_SIZE, TEST_FAVORITE_PLACE_SORT_TYPE, TEST_LAST_ID)
                    } returns createSliceFavoritePlaceResponse()
                    it("관심장소 리스트를 출력한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        )
                            .andExpect(status().isOk)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-list-success",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_USER_ID,
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                    responseBody(
                                        "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                        "content[].id" type JsonFieldType.NUMBER description "관심 장소 ID" example content.id,
                                        "content[].placeId" type JsonFieldType.STRING description "장소 ID" example content.placeId,
                                        "content[].userId" type JsonFieldType.NUMBER description "유저 ID" example content.userId,
                                        "content[].isFavoritePlace" type JsonFieldType.BOOLEAN description "관심 장소 여부" example content.isFavoritePlace,
                                    ),
                                ),
                            )
                    }
                }

                context("양수가 아닌 USERID가 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_INVALID_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        )
                            .andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-list-failed-invalid-id",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "양수가 아닌 USER ID" example TEST_INVALID_USER_ID,
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("양수가 아닌 size가 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-list-failed-invalid-size",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_USER_ID,
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "양수가 아닌 츨력할 리스트 사이즈(default=10)" example TEST_INVALID_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 USERID와 정의하지 않은 정렬 기준이 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(SORT_TYPE_PARAM, TEST_FAVORITE_PLACE_INVALID_SORT_TYPE)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-list-failed-invalid-sort-type",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_USER_ID,
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_INVALID_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "정의되지 않은 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_INVALID_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("유효한 USERID와 양수가 아닌 마지막 ID가 전달되면") {
                    it("400을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                                .param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString()),
                        ).andExpect(status().isBadRequest)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-list-failed-invalid-last-id",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_USER_ID,
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "양수가 아닌 마지막 리스트 ID" example TEST_INVALID_LAST_ID isOptional true,
                                    ),
                                ),
                            )
                    }
                }

                context("유효하지 않은 토큰이 전달되면") {
                    it("401을 반환한다.") {
                        restDocMockMvc.perform(
                            RestDocumentationRequestBuilders
                                .get(targetUri, TEST_USER_ID)
                                .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                                .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                        ).andExpect(status().isUnauthorized)
                            .andDo(
                                createPathDocument(
                                    "other-favorite-place-list-failed-invalid-token",
                                    requestHeaders(
                                        HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                    ),
                                    pathParameters(
                                        "userId" pathDescription "조회할 USER ID" example TEST_USER_ID,
                                    ),
                                    queryParameters(
                                        SIZE_PARAM pathDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                        SORT_TYPE_PARAM pathDescription "리스트 정렬기준(default=최신순)" example TEST_FAVORITE_PLACE_SORT_TYPE isOptional true,
                                        LAST_ID_PARAM pathDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
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
