package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.ImageService
import kr.weit.odya.support.BOTTOM_LATITUDE_PARAM
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.LEFT_LONGITUDE_PARAM
import kr.weit.odya.support.RIGHT_LONGITUDE_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_BOTTOM_LATITUDE
import kr.weit.odya.support.TEST_IMAGE_ID
import kr.weit.odya.support.TEST_INVALID_IMAGE_ID
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_LEFT_LONGITUDE
import kr.weit.odya.support.TEST_RIGHT_LONGITUDE
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_TOO_LONG_PHRASE
import kr.weit.odya.support.TEST_TOP_LATITUDE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.TOP_LATITUDE_PARAM
import kr.weit.odya.support.createCoordinateImageResponseList
import kr.weit.odya.support.createLifeShotRequest
import kr.weit.odya.support.createSliceImageResponse
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
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(ImageController::class)
class ImageControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val imageService: ImageService,
) : DescribeSpec(
    {

        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("GET /api/v1/images") {
            val targetUri = "/api/v1/images"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createSliceImageResponse()
                val content = response.content[0]
                every { imageService.getImages(TEST_USER_ID, TEST_SIZE, TEST_LAST_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-images-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example "null" isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                "content[].imageId" type JsonFieldType.NUMBER description "이미지 id" example content.imageId,
                                "content[].imageUrl" type JsonFieldType.STRING description "사진 URL" example content.imageUrl,
                                "content[].placeId" type JsonFieldType.STRING description "장소 id" example content.placeId isOptional true,
                                "content[].isLifeShot" type JsonFieldType.BOOLEAN description "인생샷 여부" example content.isLifeShot,
                                "content[].placeName" type JsonFieldType.STRING description "장소명" example content.placeName isOptional true,
                                "content[].journalId" type JsonFieldType.NUMBER description "여행일지 id" example content.journalId isOptional true,
                                "content[].communityId" type JsonFieldType.NUMBER description "피드 id" example content.communityId isOptional true,
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
                        param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-images-fail-invalid-last-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
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
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-images-fail-invalid-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
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
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-images-fail-not-registered-user",
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
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-images-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("POST /api/v1/images/{imageId}/life-shot") {
            val targetUri = "/api/v1/images/{imageId}/life-shot"
            val request = createLifeShotRequest()
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                every { imageService.setLifeShot(TEST_USER_ID, TEST_IMAGE_ID, request) } just runs
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .jsonContent(request)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-success",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 설정할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "장소명" example request.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 장소명이 null인 장소 설정 취소 요청인 경우") {
                val inCorrectRequest = createLifeShotRequest(placeName = null)
                every { imageService.setLifeShot(TEST_USER_ID, TEST_IMAGE_ID, inCorrectRequest) } just runs
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .jsonContent(inCorrectRequest)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-success-place-id-null",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 설정할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "장소명" example inCorrectRequest.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, image ID가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_INVALID_IMAGE_ID)
                            .jsonContent(request)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-fail-invalid-image-id",
                                pathParameters(
                                    "imageId" pathDescription "음수인 사진 id" example TEST_INVALID_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "장소명" example request.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 장소명이 공백인 경우") {
                it("400 응답한다.") {
                    val inCorrectRequest = createLifeShotRequest(placeName = " ")
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .jsonContent(inCorrectRequest)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-fail-blank-place-id",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 설정할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "공백인 장소명" example inCorrectRequest.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 장소명이 길이 제한을 넘은경우") {
                it("400 응답한다.") {
                    val request = createLifeShotRequest(placeName = TEST_TOO_LONG_PHRASE)
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .jsonContent(request)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-fail-too-long-place-id",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 설정할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "30자를 넘는 장소명" example request.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .jsonContent(request)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-fail-not-registered-user",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 설정할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "장소명" example request.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .jsonContent(request)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "set-life-shot-fail-invalid-token",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 설정할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestBody("placeName" type JsonFieldType.STRING description "장소명" example request.placeName isOptional true),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/images/{imageId}/life-shot") {
            val targetUri = "/api/v1/images/{imageId}/life-shot"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                every { imageService.cancelLifeShot(TEST_USER_ID, TEST_IMAGE_ID) } just runs
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_IMAGE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "unset-life-shot-success",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 취소할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, image ID가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_INVALID_IMAGE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "unset-life-shot-fail-invalid-image-id",
                                pathParameters(
                                    "imageId" pathDescription "음수인 사진 id" example TEST_INVALID_IMAGE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "unset-life-shot-fail-not-registered-user",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 취소할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .post(targetUri, TEST_IMAGE_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "unset-life-shot-fail-invalid-token",
                                pathParameters(
                                    "imageId" pathDescription "인생샷 취소할 사진 id" example TEST_IMAGE_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/images/coordinate") {
            val targetUri = "/api/v1/images/coordinate"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createCoordinateImageResponseList()
                val content = response[0]
                every { imageService.getImagesWithCoordinate(TEST_USER_ID, any()) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                LEFT_LONGITUDE_PARAM parameterDescription "좌측 경도" example TEST_LEFT_LONGITUDE,
                                BOTTOM_LATITUDE_PARAM parameterDescription "하단 위도" example TEST_BOTTOM_LATITUDE,
                                RIGHT_LONGITUDE_PARAM parameterDescription "우측 경도" example TEST_RIGHT_LONGITUDE,
                                TOP_LATITUDE_PARAM parameterDescription "상단 위도" example TEST_TOP_LATITUDE,
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                            ),
                            responseBody(
                                "[].imageId" type JsonFieldType.NUMBER description "이미지 id" example content.imageId,
                                "[].userId" type JsonFieldType.NUMBER description "사용자 id" example content.userId,
                                "[].imageUrl" type JsonFieldType.STRING description "사진 URL" example content.imageUrl,
                                "[].placeId" type JsonFieldType.STRING description "장소 id" example content.placeId,
                                "[].latitude" type JsonFieldType.NUMBER description "위도" example content.latitude,
                                "[].longitude" type JsonFieldType.NUMBER description "경도" example content.longitude,
                                "[].imageUserType" type JsonFieldType.STRING description "사진 사용자 타입" example content.imageUserType,
                                "[].journalId" type JsonFieldType.NUMBER description "여행일지 id" example content.journalId isOptional true,
                                "[].communityId" type JsonFieldType.NUMBER description "피드 id" example content.communityId isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 size가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-invalid-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                LEFT_LONGITUDE_PARAM parameterDescription "좌측 경도" example TEST_LEFT_LONGITUDE,
                                BOTTOM_LATITUDE_PARAM parameterDescription "하단 위도" example TEST_BOTTOM_LATITUDE,
                                RIGHT_LONGITUDE_PARAM parameterDescription "우측 경도" example TEST_RIGHT_LONGITUDE,
                                TOP_LATITUDE_PARAM parameterDescription "상단 위도" example TEST_TOP_LATITUDE,
                                SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 좌측 경도가 경도의 한계를 넘는 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, "999")
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-invalid-left-longitude",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                LEFT_LONGITUDE_PARAM parameterDescription "잘못된 좌측 경도" example "999",
                                BOTTOM_LATITUDE_PARAM parameterDescription "하단 위도" example TEST_BOTTOM_LATITUDE,
                                RIGHT_LONGITUDE_PARAM parameterDescription "우측 경도" example TEST_RIGHT_LONGITUDE,
                                TOP_LATITUDE_PARAM parameterDescription "상단 위도" example TEST_TOP_LATITUDE,
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 하단 위도가 위도의 한계를 넘는 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, "999")
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-invalid-bottom-latitude",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                LEFT_LONGITUDE_PARAM parameterDescription "좌측 경도" example TEST_LEFT_LONGITUDE,
                                BOTTOM_LATITUDE_PARAM parameterDescription "잘못된 하단 위도" example "999",
                                RIGHT_LONGITUDE_PARAM parameterDescription "우측 경도" example TEST_RIGHT_LONGITUDE,
                                TOP_LATITUDE_PARAM parameterDescription "상단 위도" example TEST_TOP_LATITUDE,
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 우측 경도가 경도의 한계를 넘는 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, "999")
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-invalid-right-longitude",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                LEFT_LONGITUDE_PARAM parameterDescription "좌측 경도" example TEST_LEFT_LONGITUDE,
                                BOTTOM_LATITUDE_PARAM parameterDescription "하단 위도" example BOTTOM_LATITUDE_PARAM,
                                RIGHT_LONGITUDE_PARAM parameterDescription "잘못된 우측 경도" example "999",
                                TOP_LATITUDE_PARAM parameterDescription "상단 위도" example TEST_TOP_LATITUDE,
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만 상단 위도가 위도의 한계를 넘는 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, "999")
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-invalid-top-latitude",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                LEFT_LONGITUDE_PARAM parameterDescription "좌측 경도" example TEST_LEFT_LONGITUDE,
                                BOTTOM_LATITUDE_PARAM parameterDescription "잘못된 하단 위도" example TEST_BOTTOM_LATITUDE,
                                RIGHT_LONGITUDE_PARAM parameterDescription "우측 경도" example TEST_RIGHT_LONGITUDE,
                                TOP_LATITUDE_PARAM parameterDescription "상단 위도" example "999",
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-not-registered-user",
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
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        param(LEFT_LONGITUDE_PARAM, TEST_LEFT_LONGITUDE.toString())
                        param(BOTTOM_LATITUDE_PARAM, TEST_BOTTOM_LATITUDE.toString())
                        param(RIGHT_LONGITUDE_PARAM, TEST_RIGHT_LONGITUDE.toString())
                        param(TOP_LATITUDE_PARAM, TEST_TOP_LATITUDE.toString())
                        param(SIZE_PARAM, TEST_SIZE.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-coordinate-images-fail-invalid-token",
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
