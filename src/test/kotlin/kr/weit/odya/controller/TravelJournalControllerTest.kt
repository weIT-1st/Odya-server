package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.service.TravelJournalService
import kr.weit.odya.service.dto.TravelJournalRequest
import kr.weit.odya.support.NOT_EXIST_USER_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_TRAVEL_CONTENT_IMAGE_MAP
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createImageNamePairs
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockOtherImageFile
import kr.weit.odya.support.createOtherTravelJournalContentRequest
import kr.weit.odya.support.createTravelJournalByTravelCompanionIdSize
import kr.weit.odya.support.createTravelJournalContentRequest
import kr.weit.odya.support.createTravelJournalContentRequestByImageNameSize
import kr.weit.odya.support.createTravelJournalRequest
import kr.weit.odya.support.createTravelJournalRequestByContentSize
import kr.weit.odya.support.createTravelJournalRequestFile
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.files
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.requestPartDescription
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.test.web.servlet.multipart
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
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns imageNamePairs
                every {
                    travelJournalService.createTravelJournal(TEST_USER_ID, travelJournalRequest, imageNamePairs)
                } just runs
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
                every { travelJournalService.getImageMap(any<List<MultipartFile>>()) } returns TEST_TRAVEL_CONTENT_IMAGE_MAP
                every {
                    travelJournalService.validateTravelJournalRequest(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } just runs
                every {
                    travelJournalService.uploadTravelContentImages(
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns imageNamePairs
                every {
                    travelJournalService.createTravelJournal(TEST_USER_ID, travelJournalRequest, imageNamePairs)
                } just runs
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
                        any<TravelJournalRequest>(),
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
                        any<TravelJournalRequest>(),
                        any<Map<String, MultipartFile>>(),
                    )
                } returns createImageNamePairs()
                every {
                    travelJournalService.createTravelJournal(
                        any<Long>(),
                        any<TravelJournalRequest>(),
                        any<List<Pair<String, String>>>(),
                    )
                } throws NoSuchElementException(
                    NOT_EXIST_USER_ERROR_MESSAGE,
                )
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

        afterEach {
            restDocumentation.afterTest()
        }
    },
)
