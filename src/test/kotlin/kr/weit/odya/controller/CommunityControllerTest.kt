package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.CommunityService
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_COMMUNITY_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunityContentImagePairs
import kr.weit.odya.support.createCommunityCreateRequest
import kr.weit.odya.support.createCommunityRequestFile
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockOtherImageFile
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.isOptional
import kr.weit.odya.support.test.requestPartDescription
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.test.web.servlet.multipart
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.multipart.MultipartFile

@UnitControllerTestEnvironment
@WebMvcTest(CommunityController::class)
class CommunityControllerTest(
    @MockkBean private val communityService: CommunityService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /api/v1/communities") {
            val targetUri = "/api/v1/communities"
            context("유효한 요청 데이터가 전달되면") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    ControllerTestHelper.jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                val communityContentImagePairs = createCommunityContentImagePairs()
                every { communityService.uploadContentImages(any<List<MultipartFile>>()) } returns communityContentImagePairs
                every { communityService.createCommunity(TEST_USER_ID, request, communityContentImagePairs) } just runs
                it("201 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                        file(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isCreated() }
                    }.andDo {
                        createDocument(
                            "community-create-success",
                            HeaderDocumentation.requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            RequestDocumentation.requestParts(
                                "community" requestPartDescription
                                    "content(String): 커뮤니티 내용(Not Null)\n " +
                                    "visibility(String): 커뮤니티 접근 권한 지정(Not Null)\n " +
                                    "placeId(String): 장소 ID(Nullable)\n " +
                                    "travelJournalId(Long): 여행 일지 아이디(Nullable)\n " +
                                    "topicId(Long): 토픽 아이디(Nullable)\n ",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("커뮤니티 콘텐츠 이미지의 원본 이름이 없는 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    ControllerTestHelper.jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                every { communityService.uploadContentImages(any<List<MultipartFile>>()) } throws IllegalArgumentException(
                    "파일 원본 이름은 필수 값입니다.",
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                        file(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME, originalFileName = null))
                        file(
                            createMockOtherImageFile(
                                mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME,
                                originalFileName = null,
                            ),
                        )
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-file-original-name-is-null",
                            HeaderDocumentation.requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            RequestDocumentation.requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "원본 이름이 없는 커뮤니티 콘텐츠 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("커뮤니티 이미지 업로드에 실패하는 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    ControllerTestHelper.jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                every {
                    communityService.uploadContentImages(
                        any<List<MultipartFile>>(),
                    )
                } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("500 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                        file(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME, originalFileName = null))
                        file(
                            createMockOtherImageFile(
                                mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME,
                                originalFileName = null,
                            ),
                        )
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-file-upload",
                            HeaderDocumentation.requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            RequestDocumentation.requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("연결하려는 여행 일지가 비공개인 경우") {
                val request = createCommunityCreateRequest(travelJournalId = 1L)
                val requestByteInputStream =
                    ControllerTestHelper.jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                val communityContentImagePairs = createCommunityContentImagePairs()
                every { communityService.uploadContentImages(any<List<MultipartFile>>()) } returns communityContentImagePairs
                every {
                    communityService.createCommunity(
                        TEST_USER_ID,
                        request,
                        communityContentImagePairs,
                    )
                } throws IllegalArgumentException(
                    "비공개 여행일지는 커뮤니티와 연결할 수 없습니다.",
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                        file(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-travel-journal-is-private",
                            HeaderDocumentation.requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            RequestDocumentation.requestParts(
                                "community" requestPartDescription "비공개 여행 일지 아이디가 포함된 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰인 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    ControllerTestHelper.jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                it("401 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        file(communityRequestFile)
                        file(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-invalid-token",
                            HeaderDocumentation.requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            RequestDocumentation.requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진" isOptional true,
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
