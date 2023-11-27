package kr.weit.odya.controller

import com.google.maps.errors.InvalidRequestException
import com.google.maps.model.PlaceDetails
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.traveljournal.TravelJournalSortType
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.service.TravelJournalService
import kr.weit.odya.service.dto.TravelJournalContentUpdateRequest
import kr.weit.odya.service.dto.TravelJournalRequest
import kr.weit.odya.service.dto.TravelJournalUpdateRequest
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.MAX_TRAVEL_COMPANION_COUNT
import kr.weit.odya.support.MAX_TRAVEL_DAYS
import kr.weit.odya.support.MAX_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.PLACE_ID_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_INVALID_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_TRAVEL_CONTENT_IMAGE_MAP
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_COUNT
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_IMAGE_NAME
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_END_DATE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_INCORRECT_DATE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_START_DATE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME
import kr.weit.odya.support.TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createImageNamePairs
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockOtherImageFile
import kr.weit.odya.support.createOtherTravelJournalContentRequest
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceDetailsMap
import kr.weit.odya.support.createSliceTaggedTravelJournalResponse
import kr.weit.odya.support.createSliceTravelJournalResponse
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournalByTravelCompanionIdSize
import kr.weit.odya.support.createTravelJournalContentRequest
import kr.weit.odya.support.createTravelJournalContentRequestByImageNameSize
import kr.weit.odya.support.createTravelJournalContentUpdateRequest
import kr.weit.odya.support.createTravelJournalRequest
import kr.weit.odya.support.createTravelJournalRequestByContentSize
import kr.weit.odya.support.createTravelJournalRequestFile
import kr.weit.odya.support.createTravelJournalResponse
import kr.weit.odya.support.createTravelJournalUpdateRequest
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.example
import kr.weit.odya.support.test.files
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.isOptional
import kr.weit.odya.support.test.parameterDescription
import kr.weit.odya.support.test.pathDescription
import kr.weit.odya.support.test.requestPartDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@UnitControllerTestEnvironment
@WebMvcTest(TravelJournalController::class)
class TravelJournalControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val travelJournalService: TravelJournalService,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/travel-journals") {
            val targetUri = "/api/v1/travel-journals"
            context("유효한 요청이 왔을 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                val imageNamePairs = createImageNamePairs()
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<List<String>>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns imageNamePairs
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns placeDetailsMap
                every {
                    travelJournalService.createTravelJournal(
                        TEST_USER_ID,
                        any<TravelJournalRequest>(),
                        any<List<Pair<String, String>>>(),
                        placeDetailsMap,
                    )
                } returns TEST_TRAVEL_JOURNAL_ID
                it("201 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription
                                    "title(String): 여행 일지 제목(Not Null)\n " +
                                    "travelStartDate(String): 여행 시작 일자(Not Null)\n " +
                                    "travelEndDate(String): 여행 종료 일자(Not Null)\n " +
                                    "visibility(String): 여행 일지 접근 권한 지정(Not Null)\n " +
                                    "travelCompanionIds(List<Number>): 친구 아이디 목록(Nullable)\n " +
                                    "travelJournalContentRequests(List<Object>): 여행 일지 콘텐츠 목록(Not Null)\n " +
                                    "travelJournalContentRequests.content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                    "travelJournalContentRequests.placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                    "travelJournalContentRequests.latitudes(List<Double>): 여행 일지 콘텐츠 위도 목록(Nullable)\n " +
                                    "travelJournalContentRequests.longitudes(List<Double>): 여행 일지 콘텐츠 경도 목록(Nullable)\n " +
                                    "travelJournalContentRequests.travelDate(String): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                    "travelJournalContentRequests.contentImageNames(List<String>): 여행 일지 콘텐츠 사진 제목 + 형식(Not NULL)\n ",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("유효한 요청이 왔을 경우(nullable한 값들이 null)") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(
                            content = null,
                            latitudes = null,
                            longitudes = null,
                            placeId = null,
                        ),
                        createOtherTravelJournalContentRequest(),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                val imageNamePairs = createImageNamePairs()
                val placeDetailsMap = emptyMap<String, PlaceDetails>()
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<List<String>>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns imageNamePairs
                every {
                    travelJournalService.createTravelJournal(
                        TEST_USER_ID,
                        any<TravelJournalRequest>(),
                        any<List<Pair<String, String>>>(),
                        placeDetailsMap,
                    )
                } returns TEST_TRAVEL_JOURNAL_ID
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns placeDetailsMap
                it("201 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "travel-journals-nullable-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "nullable인 값들이 전부 null인 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("제목이 20자가 넘는 경우") {
                val travelJournalRequest = createTravelJournalRequest(title = "가나다라마바사아자차카타파하가나다라마바사")
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-failure-title",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "20자가 넘는 제목을 가진 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠가 15개 초과인 경우") {
                val travelJournalRequest = createTravelJournalRequestByContentSize(16)
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-failure-content-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "15개가 넘는 콘텐츠를 가진 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 이미지 제목이 비어있을 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequestByImageNameSize(0),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-failure-image-name-empty",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 콘텐츠의 이미지 이름이 비어 있는 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 이미지 제목 개수가 15개를 초과하는 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequestByImageNameSize(16),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-failure-image-name-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 콘텐츠의 이미지 이름 개수가 15개를 초과하는 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 이미지가 비어있는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-image-size-empty",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 이미지가 비어있는 여행 일지",
                            ),
                        )
                    }
                }
            }

            context("여행 이미지가 225개를 초과하는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        files(226, createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-image-size-over-225",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 이미지가 225개를 초과하는 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠들의 이미지 이름 개수 보다 적은 수의 여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지의 시작일이 여행 일지의 종료일보다 늦을 경우") {
                val travelJournalRequest = createTravelJournalRequest(travelStartDate = LocalDate.parse("2021-01-03"))
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-start-date-after-travel-end-date",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 시작일이 종료일보다 이후인 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 이미지 이름 개수와 실제 이미지 개수가 다를 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mapOf(
                    TEST_IMAGE_FILE_WEBP to createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME),
                )
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-image-name-size-not-equal-to-image-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠들의 이미지 이름 개수 보다 적은 수의 여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일자보다 콘텐츠의 개수가 많을 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(),
                        createTravelJournalContentRequest(),
                        createTravelJournalContentRequest(),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-journal-content-size-more-than-travel-date-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일자보다 많은 수의 여행 일지 콘텐츠",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 콘텐츠 일자가 여행 시작일보다 이전이거나 여행 종료일보다 이후일 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(travelDate = LocalDate.parse("2020-01-01")),
                        createTravelJournalContentRequest(travelDate = LocalDate.parse("2020-01-05")),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-journal-content-travel-date-not-in-travel-date-range",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 콘텐츠 일자가 여행 시작일보다 이전이거나 여행 종료일보다 이후인 여행 일지 콘텐츠",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일자가 15일 초과인 경우") {
                val travelJournalRequest = createTravelJournalRequest(travelEndDate = LocalDate.parse("2021-01-16"))
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-end-date-over-15-days",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일자가 15일 초과인 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 친구 아이디가 등록되지 않은 사용자인 경우") {
                val travelJournalRequest = createTravelJournalRequest(travelCompanionIds = listOf(0L))
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws NoSuchElementException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("404 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-friend-not-found",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 친구의 아이디가 잘못 설정된 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 친구가 10명 초과인 경우") {
                val travelJournalRequest = createTravelJournalByTravelCompanionIdSize(11)
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-friend-over-10",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 친구가 10명이 넘는 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 이름이 실제 이미지 파일 이름과 일치하지 않는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(
                            createMockImageFile(
                                mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME,
                                originalFileName = "wrong-image-name.png",
                            ),
                        )
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-journal-content-image-name-not-equal-to-image-name",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠의 이미지 이름과 다른 여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("위도와 경도중에 하나만 null인 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(
                            latitudes = listOf(37.123456),
                            longitudes = null,
                        ),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-latitude-longitude-only-one-null",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "위도와 경도의 개수가 일치하지 않은 컨텐츠를 가진 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("위도와 경도의 개수가 다를 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(
                            latitudes = listOf(37.123456),
                            longitudes = listOf(127.123456, 127.123456),
                        ),
                    ),
                )
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws IllegalArgumentException(SOMETHING_ERROR_MESSAGE)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-latitude-longitude-size-not-equal",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "위도와 경도의 개수가 일치하지 않은 컨텐츠를 가진 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 장소 Id인 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } throws InvalidRequestException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-invalid-place-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "유효하지 않은 장소id를 가진 여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠 이미지 저장에 실패하는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<List<String>>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("500 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-travel-journal-content-image-upload-fail",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("여행 일지를 등록하려는 사용자가 없는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<List<String>>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns createImageNamePairs()
                every {
                    travelJournalService.createTravelJournal(
                        any<Long>(),
                        any<TravelJournalRequest>(),
                        any<List<Pair<String, String>>>(),
                        any<Map<String, PlaceDetails>>(),
                    )
                } throws NoSuchElementException(
                    NOT_EXIST_USER_ERROR_MESSAGE,
                )
                every { travelJournalService.getPlaceDetailsMap(setOf(TEST_PLACE_ID)) } returns createPlaceDetailsMap()
                it("404 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-user-not-found",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰일 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val travelJournalRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalRequest).byteInputStream()
                val travelJournalRequestFile =
                    createTravelJournalRequestFile(contentStream = travelJournalRequestByteInputStream)
                it("401 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        file(travelJournalRequestFile)
                        file(createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "travel-journals-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestParts(
                                "travel-journal" requestPartDescription "여행 일지",
                                "travel-journal-content-image" requestPartDescription "여행 일지 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journals/{travelJournalId}") {
            val targetUri = "/api/v1/travel-journals/{travelJournalId}"
            context("유효한 요청이 왔을 경우") {
                val response = createTravelJournalResponse()
                every { travelJournalService.getTravelJournal(TEST_TRAVEL_JOURNAL_ID, TEST_USER_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "get-travel-journal-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "조회할 여행 일지의 아이디",
                                ),
                                responseBody(
                                    "travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.travelJournalId,
                                    "title" type JsonFieldType.STRING description "여행 일지 제목" example response.title,
                                    "travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.travelStartDate,
                                    "travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.travelEndDate,
                                    "visibility" type JsonFieldType.STRING description "여행 일지 공개 범위" example response.visibility,
                                    "isBookmarked" type JsonFieldType.BOOLEAN description "여행 일지 북마크 여부" example response.isBookmarked,
                                    "writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.writer.userId,
                                    "writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.writer.nickname,
                                    "writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.writer.isFollowing,
                                    "writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.writer.profile.profileUrl,
                                    "writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.writer.profile.profileColor?.colorHex isOptional true,
                                    "writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.writer.profile.profileColor?.red isOptional true,
                                    "writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.writer.profile.profileColor?.green isOptional true,
                                    "writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.writer.profile.profileColor?.blue isOptional true,
                                    "travelJournalContents[].travelJournalContentId" type JsonFieldType.NUMBER description "여행 일지 콘텐츠 아이디" example response.travelJournalContents[0].travelJournalContentId,
                                    "travelJournalContents[].content" type JsonFieldType.STRING description "여행 일지 콘텐츠의 내용" example response.travelJournalContents[0].content,
                                    "travelJournalContents[].placeId" type JsonFieldType.STRING description "여행 일지 콘텐츠의 장소 아이디" example response.travelJournalContents[0].placeId isOptional true,
                                    "travelJournalContents[].travelDate" type JsonFieldType.STRING description "여행 일지 콘텐츠의 여행 일자" example response.travelJournalContents[0].travelDate,
                                    "travelJournalContents[].latitudes" type JsonFieldType.ARRAY description "여행 일지 콘텐츠의 위도" example response.travelJournalContents[0].latitudes isOptional true,
                                    "travelJournalContents[].longitudes" type JsonFieldType.ARRAY description "여행 일지 콘텐츠의 경도" example response.travelJournalContents[0].longitudes isOptional true,
                                    "travelJournalContents[].travelJournalContentImages[].travelJournalContentImageId" type JsonFieldType.NUMBER description "여행 일지 콘텐츠의 이미지 아이디" example response.travelJournalContents[0].travelJournalContentImages[0].travelJournalContentImageId,
                                    "travelJournalContents[].travelJournalContentImages[].contentImageName" type JsonFieldType.STRING description "여행 일지 콘텐츠의 이미지 이름" example response.travelJournalContents[0].travelJournalContentImages[0].contentImageName,
                                    "travelJournalContents[].travelJournalContentImages[].contentImageUrl" type JsonFieldType.STRING description "여행 일지 콘텐츠의 이미지 URL" example response.travelJournalContents[0].travelJournalContentImages[0].contentImageUrl,
                                    "travelJournalCompanions[].userId" type JsonFieldType.NUMBER description "여행 친구의 아이디" example response.travelJournalCompanions[0].userId isOptional true,
                                    "travelJournalCompanions[].nickname" type JsonFieldType.STRING description "여행 친구의 닉네임" example response.travelJournalCompanions[0].nickname isOptional true,
                                    "travelJournalCompanions[].profileUrl" type JsonFieldType.STRING description "여행 친구의 프로필 사진" example response.travelJournalCompanions[0].profileUrl isOptional true,
                                    "travelJournalCompanions[].isRegistered" type JsonFieldType.BOOLEAN description "여행 친구가 등록된 사용자인지 여부" example response.travelJournalCompanions[0].isRegistered,
                                ),
                            ),
                        )
                }
            }

            context("비공개 여행 일지이지만, 작성자가 요청하지 않은 경우") {
                every {
                    travelJournalService.getTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("비공개 여행 일지는 작성자만 조회할 수 있습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "get-travel-journal-fail-not-writer",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자가 아닌 사용자",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "조회할 여행 일지의 아이디",
                                ),
                            ),
                        )
                }
            }

            context("친구 공개 여행 일지이지만, 친구가 아닌 사용자가 요청한 경우") {
                every {
                    travelJournalService.getTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("친구가 아닌 사용자는 친구에게만 공개하는 여행 일지를 조회할 수 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "get-travel-journal-fail-not-friend",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "친구가 아닌 사용자",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "조회할 여행 일지의 아이디",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "get-travel-journal-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "Invalid ID Token",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "조회할 여행 일지의 아이디",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/travel-journals") {
            val targetUri = "/api/v1/travel-journals"
            context("유효한 요청이 왔을 경우") {
                val response = createSliceTravelJournalResponse()
                every {
                    travelJournalService.getTravelJournals(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        any<TravelJournalSortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].content" type JsonFieldType.STRING description "여행 일지 콘텐츠" example response.content[0].content,
                                "content[].contentImageUrl" type JsonFieldType.STRING description "여행 일지 콘텐츠의 이미지 URL" example response.content[0].contentImageUrl,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.content[0].travelEndDate,
                                "content[].placeIds[]" type JsonFieldType.ARRAY description "여행 일지 콘텐츠의 장소 아이디" example response.content[0].placeIds,
                                "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                "content[].travelCompanionSimpleResponses[].username" type JsonFieldType.STRING description "여행 친구의 아이디" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.username
                                    ) isOptional true,
                                "content[].travelCompanionSimpleResponses[].profileUrl" type JsonFieldType.STRING description "여행 친구의 프로필 사진" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.profileUrl
                                    ) isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-travel-journals-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journals/me") {
            val targetUri = "/api/v1/travel-journals/me"
            context("유효한 요청이 왔을 경우") {
                val response = createSliceTravelJournalResponse()
                every {
                    travelJournalService.getMyTravelJournals(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_PLACE_ID,
                        any<TravelJournalSortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PLACE_ID_PARAM, TEST_PLACE_ID)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-my-travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                                PLACE_ID_PARAM parameterDescription "장소 아이디" example TEST_PLACE_ID isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].content" type JsonFieldType.STRING description "여행 일지 콘텐츠" example response.content[0].content,
                                "content[].contentImageUrl" type JsonFieldType.STRING description "여행 일지 콘텐츠의 이미지 URL" example response.content[0].contentImageUrl,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.content[0].travelEndDate,
                                "content[].placeIds[]" type JsonFieldType.ARRAY description "여행 일지 콘텐츠의 장소 아이디" example response.content[0].placeIds,
                                "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                "content[].travelCompanionSimpleResponses[].username" type JsonFieldType.STRING description "여행 친구의 아이디" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.username
                                    ) isOptional true,
                                "content[].travelCompanionSimpleResponses[].profileUrl" type JsonFieldType.STRING description "여행 친구의 프로필 사진" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.profileUrl
                                    ) isOptional true,
                            ),
                        )
                    }
                }
            }

            context("placeId가 공백인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PLACE_ID_PARAM, " ")
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-my-travel-journals-fail-blank-place-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                                PLACE_ID_PARAM parameterDescription "공백인 장소 id" example " " isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-my-travel-journals-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journals/friends") {
            val targetUri = "/api/v1/travel-journals/friends"
            context("유효한 요청이 왔을 경우") {
                val response = createSliceTravelJournalResponse(
                    user = createOtherUser(),
                    travelCompanion = createTravelCompanionById(user = createUser()),
                )
                every {
                    travelJournalService.getFriendTravelJournals(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_PLACE_ID,
                        any<TravelJournalSortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PLACE_ID_PARAM, TEST_PLACE_ID)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-friend-travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                                PLACE_ID_PARAM parameterDescription "장소 아이디" example TEST_PLACE_ID isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].content" type JsonFieldType.STRING description "여행 일지 콘텐츠" example response.content[0].content,
                                "content[].contentImageUrl" type JsonFieldType.STRING description "여행 일지 콘텐츠의 이미지 URL" example response.content[0].contentImageUrl,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.content[0].travelEndDate,
                                "content[].placeIds[]" type JsonFieldType.ARRAY description "여행 일지 콘텐츠의 장소 아이디" example response.content[0].placeIds,
                                "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                "content[].travelCompanionSimpleResponses[].username" type JsonFieldType.STRING description "여행 친구의 아이디" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.username
                                    ) isOptional true,
                                "content[].travelCompanionSimpleResponses[].profileUrl" type JsonFieldType.STRING description "여행 친구의 프로필 사진" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.profileUrl
                                    ) isOptional true,
                            ),
                        )
                    }
                }
            }

            context("placeId가 공백인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PLACE_ID_PARAM, " ")
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-friend-travel-journals-fail-blank-place-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                                PLACE_ID_PARAM parameterDescription "공백인 장소 id" example " " isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-friend-travel-journals-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journals/recommends") {
            val targetUri = "/api/v1/travel-journals/recommends"
            context("유효한 요청이 왔을 경우") {
                val response = createSliceTravelJournalResponse(
                    user = createOtherUser(),
                    travelCompanion = createTravelCompanionById(user = createUser()),
                )
                every {
                    travelJournalService.getRecommendTravelJournals(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_PLACE_ID,
                        any<TravelJournalSortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PLACE_ID_PARAM, TEST_PLACE_ID)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-recommend-travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                                PLACE_ID_PARAM parameterDescription "장소 아이디" example TEST_PLACE_ID isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].content" type JsonFieldType.STRING description "여행 일지 콘텐츠" example response.content[0].content,
                                "content[].contentImageUrl" type JsonFieldType.STRING description "여행 일지 콘텐츠의 이미지 URL" example response.content[0].contentImageUrl,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].travelEndDate" type JsonFieldType.STRING description "여행 종료일" example response.content[0].travelEndDate,
                                "content[].placeIds[]" type JsonFieldType.ARRAY description "여행 일지 콘텐츠의 장소 아이디" example response.content[0].placeIds,
                                "content[].writer.userId" type JsonFieldType.NUMBER description "여행 일지 작성자의 아이디" example response.content[0].writer.userId,
                                "content[].writer.nickname" type JsonFieldType.STRING description "여행 일지 작성자의 닉네임" example response.content[0].writer.nickname,
                                "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example response.content[0].writer.isFollowing,
                                "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 사진" example response.content[0].writer.profile.profileUrl,
                                "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "여행 일지 작성자의 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "여행 일지 작성자의 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                "content[].travelCompanionSimpleResponses[].username" type JsonFieldType.STRING description "여행 친구의 아이디" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.username
                                    ) isOptional true,
                                "content[].travelCompanionSimpleResponses[].profileUrl" type JsonFieldType.STRING description "여행 친구의 프로필 사진" example (
                                    response.content[0].travelCompanionSimpleResponses?.get(0)?.profileUrl
                                    ) isOptional true,
                            ),
                        )
                    }
                }
            }

            context("placeId가 공백인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PLACE_ID_PARAM, " ")
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "get-recommend-travel-journals-fail-blank-place-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                                PLACE_ID_PARAM parameterDescription "공백인 장소 id" example " " isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-recommend-travel-journals-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/travel-journals/tagged") {
            val targetUri = "/api/v1/travel-journals/tagged"
            context("유효한 요청이 왔을 경우") {
                val response = createSliceTaggedTravelJournalResponse()
                every {
                    travelJournalService.getTaggedTravelJournals(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-tagged-travel-journals-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 여부" example response.hasNext,
                                "content[].travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalId,
                                "content[].title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].title,
                                "content[].travelStartDate" type JsonFieldType.STRING description "여행 시작일" example response.content[0].travelStartDate,
                                "content[].mainImageUrl" type JsonFieldType.STRING description "여행 일지의 대표 이미지 URL" example response.content[0].mainImageUrl,
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
                            "get-tagged-travel-journals-fail-invalid-last-id",
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
                            "get-tagged-travel-journals-fail-invalid-size",
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

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-tagged-travel-journals-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "데이터 개수 (default = 10)" example TEST_DEFAULT_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 여행 일지 ID" example TEST_TRAVEL_JOURNAL_ID isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("PUT /api/v1/travel-journals/{travelJournalId}") {
            val targetUri = "/api/v1/travel-journals/{travelJournalId}"
            context("유효한 요청이 왔을 경우") {
                val travelJournalUpdateRequest = createTravelJournalUpdateRequest()
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("작성자가 아닌 사용자가 요청한 경우") {
                val travelJournalUpdateRequest = createTravelJournalUpdateRequest()
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 요청을 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-not-same-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자가 아닌 사용자 ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("여행 시작일이 여행 종료일보다 늦는 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelStartDate = LocalDate.of(2022, 1, 11))
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지의 시작일(${travelJournalUpdateRequest.travelStartDate})은 종료일(${travelJournalUpdateRequest.travelEndDate})보다 이전이거나 같아야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-later-start-date",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 종료일보다 늦는 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("여행 기간이 제한 기간보다 긴 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelEndDate = LocalDate.of(2022, 1, 16))
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지의 여행 기간(${travelJournalUpdateRequest.travelDurationDays})은 ${MAX_TRAVEL_DAYS}일 이하이어야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-longer-than-max-days",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 제한 범위를 벗어나는 범위의 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 제한 범위를 벗어나는 범위의 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("여행 일지 기간과 여행 일지 콘텐츠 개수가 다른 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelEndDate = LocalDate.of(2022, 1, 1))
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지의 여행 기간(${travelJournalUpdateRequest.travelDurationDays})은 여행 일지 콘텐츠의 개수($TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_COUNT)보다 크거나 같아야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-not-same-content-count",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 여행 일지 콘텐츠 개수와 일치하지 않는 범위의 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 일지 콘텐츠 개수와 일치하지 않는 범위의 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("여행 일지 콘텐츠의 날짜와 여행 시작일, 종료일 사이에 없는 경우") {
                val travelJournalUpdateRequest = createTravelJournalUpdateRequest(
                    travelStartDate = LocalDate.of(2022, 2, 1),
                    travelEndDate = LocalDate.of(2022, 2, 1),
                )
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지 콘텐츠의 여행 일자($TEST_TRAVEL_JOURNAL_INCORRECT_DATE)는 여행 일지의 시작일(${travelJournalUpdateRequest.travelStartDate})과 종료일(${travelJournalUpdateRequest.travelEndDate}) 사이여야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-content-date-not-between-start-end-date",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 여행 일지 콘텐츠 여행 일자 범위 밖의 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 일지 콘텐츠 여행 일자 범위 밖의 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("여행 친구 수가 제한 친구 수보다 많은 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelCompanionIds = (0..10).map { it.toLong() })
                val travelJournalUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalUpdateRequestByteInputStream,
                    )
                every {
                    travelJournalService.updateTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                        any<TravelJournalUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지 친구는 최대 ${MAX_TRAVEL_COMPANION_COUNT}명까지 등록 가능합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-over-max-companion-count",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 제한 범위를 넘는 수의 여행 친구 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    val travelJournalUpdateRequest = createTravelJournalUpdateRequest()
                    val travelJournalUpdateRequestByteInputStream =
                        ControllerTestHelper.jsonContent(travelJournalUpdateRequest).byteInputStream()
                    val travelJournalUpdateRequestFile =
                        createTravelJournalRequestFile(
                            name = TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME,
                            contentStream = travelJournalUpdateRequestByteInputStream,
                        )
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .file(travelJournalUpdateRequestFile)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-update-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                ),
                                requestParts(
                                    "travel-journal-update" requestPartDescription
                                        "title(String): 여행 일지 제목(Not Null)\n " +
                                        "travelStartDate(String): 여행 시작일(Not Null)\n " +
                                        "travelEndDate(String): 여행 종료일(Not Null)\n " +
                                        "visibility(String): 여행 일지 공개 범위(Not Null)\n " +
                                        "travelCompanionIds(List<Long>): 여행 친구의 아이디 목록(Nullable)\n " +
                                        "travelCompanionNames(List<String>): 여행 친구의 닉네임 목록(Nullable)\n ",
                                ),
                            ),
                        )
                }
            }
        }

        describe("PUT /api/v1/travel-journals/{travelJournalId}/{travelJournalContentId}") {
            val targetUri = "/api/v1/travel-journals/{travelJournalId}/{travelJournalContentId}"
            context("유효한 요청이 왔을 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every { travelJournalService.validateTravelJournalContentUpdateRequest(TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID, TEST_USER_ID, any<Map<String, MultipartFile>>(), any<TravelJournalContentUpdateRequest>()) } just runs
                every { travelJournalService.uploadTravelContentImages(any<List<String>>(), any<Map<String, MultipartFile>>()) } returns mockk<List<Pair<String, String>>>()
                every { travelJournalService.updateTravelJournalContent(TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID, TEST_USER_ID, any<TravelJournalContentUpdateRequest>(), any<List<Pair<String, String>>>(), any<Map<String, PlaceDetails>>()) } just runs
                every { travelJournalService.getPlaceDetailsMap(any()) } returns createPlaceDetailsMap()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<List<String>>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns mockk<List<Pair<String, String>>>()
                every {
                    travelJournalService.updateTravelJournalContent(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<TravelJournalContentUpdateRequest>(),
                        any<List<Pair<String, String>>>(),
                        any<Map<String, PlaceDetails>>(),
                    )
                } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-contents-update-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "수정할 여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 추가할 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("여행 일지 콘텐츠 아이디에 맞는 여행 일지 콘텐츠가 없는 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } throws NoSuchElementException("해당 여행 일지 콘텐츠($TEST_TRAVEL_JOURNAL_CONTENT_ID)가 존재하지 않습니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-not-found-content",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "수정할 없는 여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 추가할 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("작성자와 요청자가 다른 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 요청을 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-not-same-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자가 아닌 사용자 ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 추가할 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("여행 일지 콘텐츠의 이미지 개수가 제한 개수보다 많은 경우") {
                val travelJournalContentUpdateRequest =
                    createTravelJournalContentUpdateRequest(updateContentImageNames = (0..16).map { "$it.webp" })
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지 콘텐츠의 이미지 개수(0)와 추가 이미지 개수(${travelJournalContentUpdateRequest.updateImageTotalCount})의 합은 최대 $MAX_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT 개까지 등록 가능합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-over-max-image-count",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 제한 범위 이상의 추가할 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("추가할 여행 일지 콘텐츠의 이름과 이미지 파일의 이름이 다른 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    updateContentImageNames = listOf(TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_IMAGE_NAME),
                )
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } throws IllegalArgumentException("추가할 여행 일지 콘텐츠의 이미지 이름($TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_IMAGE_NAME)은 여행 이미지 파일 이름과 일치해야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-not-same-image-name",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이름과 일치하지 않는 추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("여행 일지 콘텐츠 여행 일자가 여행 시작일과 종료일 사이에 없는 경우") {
                val travelJournalContentUpdateRequest =
                    createTravelJournalContentUpdateRequest(travelDate = LocalDate.of(2023, 1, 1))
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지 콘텐츠의 여행 일자(${travelJournalContentUpdateRequest.travelDate})는 여행 일지의 시작일($TEST_TRAVEL_JOURNAL_START_DATE)과 종료일($TEST_TRAVEL_JOURNAL_END_DATE) 사이여야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-travel-date-not-between-start-date-and-end-date",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 시작일과 종료일 사이에 없는 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("위도와 경도의 개수가 다른 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    latitudes = listOf(37.1234, 37.1234),
                    longitudes = listOf(127.1234),
                )
                val travelJournalContentUpdateRequestByteInputStream =
                    ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                val travelJournalUpdateRequestFile =
                    createTravelJournalRequestFile(
                        name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                        contentStream = travelJournalContentUpdateRequestByteInputStream,
                    )
                val placeDetailsMap = createPlaceDetailsMap()
                every { travelJournalService.getPlaceDetailsMap(any()) } returns placeDetailsMap
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns mockk<Map<String, MultipartFile>>()
                every {
                    travelJournalService.validateTravelJournalContentUpdateRequest(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                        any<Map<String, MultipartFile>>(),
                        any<TravelJournalContentUpdateRequest>(),
                    )
                } throws IllegalArgumentException("여행 일지 콘텐츠의 위도 개수(${travelJournalContentUpdateRequest.latitudes?.size})와 경도 개수(${travelJournalContentUpdateRequest.longitudes?.size})는 같아야 합니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-not-same-size-of-latitudes-and-longitudes",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 경도 목록과 사이즈가 다른 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 위도 목록과 사이즈가 다른 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                    val travelJournalContentUpdateRequestByteInputStream =
                        ControllerTestHelper.jsonContent(travelJournalContentUpdateRequest).byteInputStream()
                    val travelJournalUpdateRequestFile =
                        createTravelJournalRequestFile(
                            name = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME,
                            contentStream = travelJournalContentUpdateRequestByteInputStream,
                        )
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .file(travelJournalUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .file(
                                createMockImageFile(
                                    TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
                                ),
                            )
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                            .with {
                                it.method = "PUT"
                                it
                            },
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-update-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "수정할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "여행 일지의 콘텐츠 아이디",
                                ),
                                requestParts(
                                    "travel-journal-content-update" requestPartDescription
                                        "content(String): 여행 일지 콘텐츠 내용(Nullable)\n " +
                                        "placeId(String): 여행 일지 콘텐츠 장소 아이디(Nullable)\n " +
                                        "latitudes(List<Double>): 경도 목록과 사이즈가 다른 위도 목록(Nullable)\n " +
                                        "longitudes(List<Double>): 위도 목록과 사이즈가 다른 경도 목록(Nullable)\n " +
                                        "travelDate(LocalDate): 여행 일지 콘텐츠 일자(Not Null)\n " +
                                        "updateContentImageNames(List<String>): 여행 일지 콘텐츠 이미지 이름(Nullable)\n " +
                                        "deleteContentImageIds(List<Long>): 삭제할 여행 일지 콘텐츠 이미지 아이디(Nullable)\n ",
                                    "travel-journal-content-image-update" requestPartDescription "추가할 여행 일지 콘텐츠 이미지" isOptional true,
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/travel-journals/{travelJournalId}") {
            val targetUri = "/api/v1/travel-journals/{travelJournalId}"
            context("유효한 요청이 왔을 경우") {
                every { travelJournalService.deleteTravelJournal(TEST_TRAVEL_JOURNAL_ID, TEST_USER_ID) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-delete-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                ),
                            ),
                        )
                }
            }

            context("작성자와 요청자가 다른 경우") {
                every {
                    travelJournalService.deleteTravelJournal(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 요청을 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-delete-fail-not-same-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자가 아닌 사용자 ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journals-delete-fail-invalid-token",
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/travel-journals/{travelJournalId}/{travelJournalContentId}") {
            val targetUri = "/api/v1/travel-journals/{travelJournalId}/{travelJournalContentId}"
            context("유효한 요청이 왔을 경우") {
                every {
                    travelJournalService.deleteTravelJournalContent(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                    )
                } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-delete-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "삭제할 여행 일지의 콘텐츠 아이디",
                                ),
                            ),
                        )
                }
            }

            context("작성자와 요청자가 다른 경우") {
                every {
                    travelJournalService.deleteTravelJournalContent(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 요청을 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-delete-fail-not-same-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자와 다른 사용자 ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "삭제할 여행 일지의 콘텐츠 아이디",
                                ),
                            ),
                        )
                }
            }

            context("여행 일지 콘텐츠 아이디와 일치하는 여행 일지 콘텐츠가 없는 경우") {
                every {
                    travelJournalService.deleteTravelJournalContent(
                        TEST_TRAVEL_JOURNAL_ID,
                        TEST_TRAVEL_JOURNAL_CONTENT_ID,
                        TEST_USER_ID,
                    )
                } throws NoSuchElementException("해당 여행 일지 콘텐츠($TEST_TRAVEL_JOURNAL_CONTENT_ID)가 존재하지 않습니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-delete-fail-not-found",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "삭제할 없는 여행 일지의 콘텐츠 아이디",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID, TEST_TRAVEL_JOURNAL_CONTENT_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-content-delete-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디",
                                    "travelJournalContentId" pathDescription "삭제할 여행 일지의 콘텐츠 아이디",
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/travel-journals/{travelJournalId}/travelCompanion") {
            val targetUri = "/api/v1/travel-journals/{travelJournalId}/travelCompanion"
            context("유효한 요청이 왔을 경우") {
                every { travelJournalService.removeTravelCompanion(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-remove-travel-companion-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디" example TEST_TRAVEL_JOURNAL_ID,
                                ),
                            ),
                        )
                }
            }

            context("양수가 아닌 여행 일지 아이디가 들어온 경우") {
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_INVALID_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-remove-travel-companion-fail-invalid-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "양수가 아닌 여행 일지의 아이디" example TEST_INVALID_TRAVEL_JOURNAL_ID,
                                ),
                            ),
                        )
                }
            }

            context("해당 여행일지의 같이 간 친구를 처리할 권한이 없는 경우") {
                every { travelJournalService.removeTravelCompanion(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID) } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 여행일지($TEST_TRAVEL_JOURNAL_ID)의 같이 간 친구를 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-remove-travel-companion-fail-no-permission",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "처리할 권한이 없는 사용자의 ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디" example TEST_TRAVEL_JOURNAL_ID,
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 유저ID가 들어온 경우") {
                every { travelJournalService.removeTravelCompanion(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID) } throws NoSuchElementException(NOT_EXIST_USER_ERROR_MESSAGE)
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-remove-travel-companion-fail-not-found",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디" example TEST_TRAVEL_JOURNAL_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰일 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_TRAVEL_JOURNAL_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            RestDocsHelper.createPathDocument(
                                "travel-journal-remove-travel-companion-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "travelJournalId" pathDescription "삭제할 여행 일지의 아이디" example TEST_TRAVEL_JOURNAL_ID,
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
