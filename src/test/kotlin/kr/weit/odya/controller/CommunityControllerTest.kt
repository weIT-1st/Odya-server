package kr.weit.odya.controller

import com.google.maps.errors.InvalidRequestException
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.community.CommunitySortType
import kr.weit.odya.service.CommunityService
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.service.dto.CommunityUpdateRequest
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.MAX_COMMUNITY_CONTENT_IMAGE_COUNT
import kr.weit.odya.support.MIM_COMMUNITY_CONTENT_IMAGE_COUNT
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.SORT_TYPE_PARAM
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_COMMUNITY_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_COMMUNITY_UPDATE_REQUEST_NAME
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_NOT_EXIST_TOPIC_ID
import kr.weit.odya.support.TEST_NOT_EXIST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_UPDATE_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunityContentImagePairs
import kr.weit.odya.support.createCommunityCreateRequest
import kr.weit.odya.support.createCommunityRequestFile
import kr.weit.odya.support.createCommunityResponse
import kr.weit.odya.support.createCommunityUpdateRequest
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockOtherImageFile
import kr.weit.odya.support.createSliceCommunitySimpleResponse
import kr.weit.odya.support.createSliceCommunitySummaryResponse
import kr.weit.odya.support.createUpdateCommunityContentImagePairs
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
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
                    jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                val communityContentImagePairs = createCommunityContentImagePairs()
                every { communityService.uploadContentImages(any<List<MultipartFile>>()) } returns communityContentImagePairs
                every {
                    communityService.createCommunity(
                        TEST_USER_ID,
                        request,
                        communityContentImagePairs,
                    )
                } returns TEST_COMMUNITY_ID
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
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription
                                    "content(String): 커뮤니티 내용(Not Null)\n " +
                                    "visibility(String): 커뮤니티 접근 권한 지정(Not Null)\n " +
                                    "placeId(String): 장소 ID(Nullable)\n " +
                                    "travelJournalId(Long): 여행 일지 아이디(Nullable)\n " +
                                    "topicId(Long): 토픽 아이디(Nullable)\n ",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("커뮤니티 콘텐츠 이미지가 없을 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-file-is-null",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                            ),
                        )
                    }
                }
            }

            context("커뮤니티 콘텐츠 이미지가 15개를 넘을 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                        files(16, createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-over-size-files",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "15개가 넘는 커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("커뮤니티 콘텐츠 이미지의 원본 이름이 없는 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
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
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "원본 이름이 없는 커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("커뮤니티 이미지 업로드에 실패하는 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
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
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("연결하려는 여행 일지가 비공개인 경우") {
                val request = createCommunityCreateRequest(travelJournalId = 1L)
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
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
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "비공개 여행 일지 아이디가 포함된 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("태그된 장소id가 유효하지 않은 경우") {
                val request = createCommunityCreateRequest(travelJournalId = 1L)
                val requestByteInputStream = jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                val communityContentImagePairs = createCommunityContentImagePairs()
                every { communityService.uploadContentImages(any<List<MultipartFile>>()) } returns communityContentImagePairs
                every {
                    communityService.createCommunity(
                        TEST_USER_ID,
                        request,
                        communityContentImagePairs,
                    )
                } throws InvalidRequestException(SOMETHING_ERROR_MESSAGE)
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
                            "community-create-fail-not-exist-place-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "유효하지 않은 장소id가 포함된 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("요청 사용자가 작성한 여행 일지와 연결하지 않는 경우") {
                val request = createCommunityCreateRequest(travelJournalId = 1L)
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
                val communityRequestFile = createCommunityRequestFile(contentStream = requestByteInputStream)
                val communityContentImagePairs = createCommunityContentImagePairs()
                every { communityService.uploadContentImages(any<List<MultipartFile>>()) } returns communityContentImagePairs
                every {
                    communityService.createCommunity(
                        TEST_USER_ID,
                        request,
                        communityContentImagePairs,
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 여행 일지를 커뮤니티에 연결할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.POST, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(communityRequestFile)
                        file(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                        file(createMockOtherImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME))
                    }.andExpect {
                        status { isForbidden() }
                    }.andDo {
                        createDocument(
                            "community-create-fail-travel-journal-is-not-mine",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "본인이 작성하지 않은 여행 일지 아이디가 포함된 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰인 경우") {
                val request = createCommunityCreateRequest()
                val requestByteInputStream =
                    jsonContent(request).byteInputStream()
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
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestParts(
                                "community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                "community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/communities/{communityId}") {
            val targetUri = "/api/v1/communities/{communityId}"
            context("유효한 요청 데이터가 전달되면") {
                val response = createCommunityResponse()
                every {
                    communityService.getCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isOk)
                        .andDo(
                            createPathDocument(
                                "community-get-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                responseBody(
                                    "communityId" type JsonFieldType.NUMBER description "커뮤니티 아이디" example response.communityId,
                                    "content" type JsonFieldType.STRING description "커뮤니티 내용" example response.content,
                                    "visibility" type JsonFieldType.STRING description "커뮤니티 접근 권한" example response.visibility,
                                    "placeId" type JsonFieldType.STRING description "장소 아이디" example response.placeId isOptional true,
                                    "travelJournal.travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.travelJournal?.travelJournalId isOptional true,
                                    "travelJournal.title" type JsonFieldType.STRING description "여행 일지 제목" example response.travelJournal?.title isOptional true,
                                    "travelJournal.mainImageUrl" type JsonFieldType.STRING description "여행 일지 대표 이미지 URL" example response.travelJournal?.mainImageUrl isOptional true,
                                    "travelJournal.mainImageUrl" type JsonFieldType.STRING description "여행 일지 대표 이미지 URL" example response.travelJournal?.mainImageUrl isOptional true,
                                    "topic.id" type JsonFieldType.NUMBER description "토픽 아이디" example response.topic?.id isOptional true,
                                    "topic.topic" type JsonFieldType.STRING description "토픽 정보" example response.topic?.topic isOptional true,
                                    "communityContentImages[].communityContentImageId" type JsonFieldType.NUMBER description "커뮤니티 콘텐츠 이미지 아이디" example response.communityContentImages[0].communityContentImageId,
                                    "communityContentImages[].imageUrl" type JsonFieldType.STRING description "커뮤니티 콘텐츠 이미지 URL" example response.communityContentImages[0].imageUrl,
                                    "communityCommentCount" type JsonFieldType.NUMBER description "커뮤니티 댓글 수" example response.communityCommentCount,
                                    "communityLikeCount" type JsonFieldType.NUMBER description "커뮤니티 좋아요 수" example response.communityLikeCount,
                                    "isUserLiked" type JsonFieldType.BOOLEAN description "사용자가 좋아요를 눌렀는지 여부" example response.isUserLiked,
                                ),
                            ),
                        )
                }
            }

            context("요청한 커뮤니티 아이디의 커뮤니티가 없는 경우") {
                every {
                    communityService.getCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws NoSuchElementException("커뮤니티 아이디($TEST_COMMUNITY_ID)에 해당하는 커뮤니티가 없습니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "community-get-fail-not-found",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "존재하지 않는 커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }

            context("친구가 아닌 사용자가 요청한 경우") {
                every {
                    communityService.getCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("친구가 아닌 사용자($TEST_USER_ID)는 친구에게만 공개하는 여행 일지($TEST_COMMUNITY_ID)를 조회할 수 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "community-get-fail-not-friend",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "친구가 아닌 사용자의 ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "community-get-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/communities") {
            val targetUri = "/api/v1/communities"
            context("유효한 요청 데이터가 전달되면") {
                val response = createSliceCommunitySummaryResponse()
                every {
                    communityService.getCommunities(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        any<CommunitySortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }
                        .andExpect {
                            status { isOk() }
                        }
                        .andDo {
                            createDocument(
                                "community-get-communities-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example CommunitySortType.values() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 존재 여부" example response.hasNext,
                                    "content[].communityId" type JsonFieldType.NUMBER description "커뮤니티 아이디" example response.content[0].communityId,
                                    "content[].communityContent" type JsonFieldType.STRING description "커뮤니티 내용" example response.content[0].communityContent,
                                    "content[].communityMainImageUrl" type JsonFieldType.STRING description "커뮤니티 대표 이미지 URL" example response.content[0].communityMainImageUrl,
                                    "content[].placeId" type JsonFieldType.STRING description "장소 아이디" example response.content[0].placeId isOptional true,
                                    "content[].writer.userId" type JsonFieldType.NUMBER description "작성자 아이디" example response.content[0].writer.userId,
                                    "content[].writer.nickname" type JsonFieldType.STRING description "작성자 닉네임" example response.content[0].writer.nickname,
                                    "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "작성자를 팔로우 중인지 여부" example response.content[0].writer.isFollowing,
                                    "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "작성자 프로필 이미지 URL" example response.content[0].writer.profile.profileUrl,
                                    "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "작성자 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                    "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "작성자 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                    "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "작성자 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                    "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "작성자 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                    "content[].travelJournalSimpleResponse.travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalSimpleResponse?.travelJournalId isOptional true,
                                    "content[].travelJournalSimpleResponse.title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].travelJournalSimpleResponse?.title isOptional true,
                                    "content[].travelJournalSimpleResponse.mainImageUrl" type JsonFieldType.STRING description "여행 일지 대표 이미지 URL" example response.content[0].travelJournalSimpleResponse?.mainImageUrl isOptional true,
                                    "content[].communityCommentCount" type JsonFieldType.NUMBER description "커뮤니티 댓글 수" example response.content[0].communityCommentCount,
                                    "content[].communityLikeCount" type JsonFieldType.NUMBER description "커뮤니티 좋아요 수" example response.content[0].communityLikeCount,
                                ),
                            )
                        }
                }
            }

            context("유효하지 않은 토큰인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }
                        .andExpect {
                            status { isUnauthorized() }
                        }
                        .andDo {
                            createDocument(
                                "community-get-communities-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example CommunitySortType.values() isOptional true,
                                ),
                            )
                        }
                }
            }
        }

        describe("GET /api/v1/communities/me") {
            val targetUri = "/api/v1/communities/me"
            context("유효한 요청 데이터가 전달되면") {
                val response = createSliceCommunitySimpleResponse()
                every {
                    communityService.getMyCommunities(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        any<CommunitySortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }
                        .andExpect {
                            status { isOk() }
                        }
                        .andDo {
                            createDocument(
                                "community-get-my-communities-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example CommunitySortType.values() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 존재 여부" example response.hasNext,
                                    "content[].communityId" type JsonFieldType.NUMBER description "커뮤니티 아이디" example response.content[0].communityId,
                                    "content[].communityMainImageUrl" type JsonFieldType.STRING description "커뮤니티 대표 이미지 URL" example response.content[0].communityMainImageUrl,
                                    "content[].placeId" type JsonFieldType.STRING description "장소 아이디" example response.content[0].placeId isOptional true,
                                ),
                            )
                        }
                }
            }

            context("유효하지 않은 토큰인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }
                        .andExpect {
                            status { isUnauthorized() }
                        }
                        .andDo {
                            createDocument(
                                "community-get-my-communities-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example CommunitySortType.values() isOptional true,
                                ),
                            )
                        }
                }
            }
        }

        describe("GET /api/v1/communities/friends") {
            val targetUri = "/api/v1/communities/friends"
            context("유효한 요청 데이터가 전달되면") {
                val response = createSliceCommunitySummaryResponse()
                every {
                    communityService.getFriendCommunities(
                        TEST_USER_ID,
                        TEST_DEFAULT_SIZE,
                        null,
                        any<CommunitySortType>(),
                    )
                } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }
                        .andExpect {
                            status { isOk() }
                        }
                        .andDo {
                            createDocument(
                                "community-get-friend-communities-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example CommunitySortType.values() isOptional true,
                                ),
                                responseBody(
                                    "hasNext" type JsonFieldType.BOOLEAN description "다음 페이지 존재 여부" example response.hasNext,
                                    "content[].communityId" type JsonFieldType.NUMBER description "커뮤니티 아이디" example response.content[0].communityId,
                                    "content[].communityContent" type JsonFieldType.STRING description "커뮤니티 내용" example response.content[0].communityContent,
                                    "content[].communityMainImageUrl" type JsonFieldType.STRING description "커뮤니티 대표 이미지 URL" example response.content[0].communityMainImageUrl,
                                    "content[].placeId" type JsonFieldType.STRING description "장소 아이디" example response.content[0].placeId isOptional true,
                                    "content[].writer.userId" type JsonFieldType.NUMBER description "작성자 아이디" example response.content[0].writer.userId,
                                    "content[].writer.nickname" type JsonFieldType.STRING description "작성자 닉네임" example response.content[0].writer.nickname,
                                    "content[].writer.isFollowing" type JsonFieldType.BOOLEAN description "작성자를 팔로우 중인지 여부" example response.content[0].writer.isFollowing,
                                    "content[].writer.profile.profileUrl" type JsonFieldType.STRING description "작성자 프로필 이미지 URL" example response.content[0].writer.profile.profileUrl,
                                    "content[].writer.profile.profileColor.colorHex" type JsonFieldType.STRING description "작성자 프로필 색상" example response.content[0].writer.profile.profileColor?.colorHex isOptional true,
                                    "content[].writer.profile.profileColor.red" type JsonFieldType.NUMBER description "작성자 프로필 색상의 빨간색 값" example response.content[0].writer.profile.profileColor?.red isOptional true,
                                    "content[].writer.profile.profileColor.green" type JsonFieldType.NUMBER description "작성자 프로필 색상의 초록색 값" example response.content[0].writer.profile.profileColor?.green isOptional true,
                                    "content[].writer.profile.profileColor.blue" type JsonFieldType.NUMBER description "작성자 프로필 색상의 파란색 값" example response.content[0].writer.profile.profileColor?.blue isOptional true,
                                    "content[].travelJournalSimpleResponse.travelJournalId" type JsonFieldType.NUMBER description "여행 일지 아이디" example response.content[0].travelJournalSimpleResponse?.travelJournalId isOptional true,
                                    "content[].travelJournalSimpleResponse.title" type JsonFieldType.STRING description "여행 일지 제목" example response.content[0].travelJournalSimpleResponse?.title isOptional true,
                                    "content[].travelJournalSimpleResponse.mainImageUrl" type JsonFieldType.STRING description "여행 일지 대표 이미지 URL" example response.content[0].travelJournalSimpleResponse?.mainImageUrl isOptional true,
                                    "content[].communityCommentCount" type JsonFieldType.NUMBER description "커뮤니티 댓글 수" example response.content[0].communityCommentCount,
                                    "content[].communityLikeCount" type JsonFieldType.NUMBER description "커뮤니티 좋아요 수" example response.content[0].communityLikeCount,
                                ),
                            )
                        }
                }
            }

            context("유효하지 않은 토큰인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }
                        .andExpect {
                            status { isUnauthorized() }
                        }
                        .andDo {
                            createDocument(
                                "community-get-friend-communities-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_DEFAULT_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                    SORT_TYPE_PARAM parameterDescription "정렬 타입" example CommunitySortType.values() isOptional true,
                                ),
                            )
                        }
                }
            }
        }

        describe("PUT /api/v1/communities/{communityId}") {
            val targetUri = "/api/v1/communities/{communityId}"
            context("유효한 요청 데이터가 전달되면") {
                val communityUpdateRequest = createCommunityUpdateRequest()
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                every {
                    communityService.validateUpdateCommunityRequest(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<List<Long>>(),
                        any<Int>(),
                    )
                } just runs
                every {
                    communityService.uploadContentImages(any<List<MultipartFile>>())
                } returns createUpdateCommunityContentImagePairs()
                every {
                    communityService.updateCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<CommunityUpdateRequest>(),
                        any<List<Pair<String, String>>>(),
                    )
                } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription
                                        "content(String): 커뮤니티 내용(Not Null)\n " +
                                        "visibility(String): 커뮤니티 접근 권한(Not Null)\n " +
                                        "placeId(String): 장소 아이디(Nullable)\n " +
                                        "travelJournalId(Long): 여행 일지 아이디(Nullable)\n " +
                                        "topicId(Long): 토픽 아이디(Nullable)\n " +
                                        "deleteCommunityContentImageIds(List<Long>): 삭제할 커뮤티니 컨텐츠 이미지 아이디(Nullable)\n ",
                                    "update-community-content-image" requestPartDescription "추가할 커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 커뮤니티 아이디가 주어지는 경우") {
                val communityUpdateRequest = createCommunityUpdateRequest()
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                every {
                    communityService.validateUpdateCommunityRequest(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<List<Long>>(),
                        any<Int>(),
                    )
                } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-fail-not-found",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "존재하지 않는 커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                    "update-community-content-image" requestPartDescription "추가할 커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }

            context("수정 요청자가 작성자와 다른 경우") {
                val communityUpdateRequest = createCommunityUpdateRequest()
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                every {
                    communityService.validateUpdateCommunityRequest(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<List<Long>>(),
                        any<Int>(),
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 요청을 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-fail-not-writer",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자가 아닌 사용자의 ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                    "update-community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }

            context("커뮤니티 이미지 개수가 제한 범위를 벗어나는 경우") {
                val communityUpdateRequest =
                    createCommunityUpdateRequest(deleteCommunityContentImageIds = (1..3).map { it.toLong() })
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                every {
                    communityService.validateUpdateCommunityRequest(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<List<Long>>(),
                        any<Int>(),
                    )
                } throws IllegalArgumentException("커뮤니티 사진은 최소 $MIM_COMMUNITY_CONTENT_IMAGE_COUNT 개 이상, 최대 $MAX_COMMUNITY_CONTENT_IMAGE_COUNT 개 이하로 업로드할 수 있습니다.")
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-fail-exceed-max-community-content-image-count",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription "제한 범위를 벗어난 삭제 이미지 아이디를 포함한 커뮤니티 요청 데이터",
                                    "update-community-content-image" requestPartDescription "제한 범위를 벗어난 추가 커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 여행 일지 아이디가 주어지는 경우") {
                val communityUpdateRequest =
                    createCommunityUpdateRequest(travelJournalId = TEST_NOT_EXIST_TRAVEL_JOURNAL_ID)
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                every {
                    communityService.validateUpdateCommunityRequest(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<List<Long>>(),
                        any<Int>(),
                    )
                } just runs
                every {
                    communityService.uploadContentImages(any<List<MultipartFile>>())
                } returns createUpdateCommunityContentImagePairs()
                every {
                    communityService.updateCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<CommunityUpdateRequest>(),
                        any<List<Pair<String, String>>>(),
                    )
                } throws NoSuchElementException("$TEST_NOT_EXIST_TRAVEL_JOURNAL_ID: 존재하지 않는 여행 일지입니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-fail-not-found-travel-journal",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription "존재하지 않는 여행 일지 아이디를 포함한 커뮤니티 요청 데이터",
                                    "update-community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 토픽 아이디가 주어지는 경우") {
                val communityUpdateRequest =
                    createCommunityUpdateRequest(topicId = TEST_NOT_EXIST_TOPIC_ID)
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                every {
                    communityService.validateUpdateCommunityRequest(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<List<Long>>(),
                        any<Int>(),
                    )
                } just runs
                every {
                    communityService.uploadContentImages(any<List<MultipartFile>>())
                } returns createUpdateCommunityContentImagePairs()
                every {
                    communityService.updateCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                        any<CommunityUpdateRequest>(),
                        any<List<Pair<String, String>>>(),
                    )
                } throws NoSuchElementException("$TEST_NOT_EXIST_TOPIC_ID : 해당 토픽이 존재하지 않습니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-fail-not-found-topic",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription "존재하지 않는 토픽 아이디를 포함한 커뮤니티 요청 데이터",
                                    "update-community-content-image" requestPartDescription "커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰인 경우") {
                val communityUpdateRequest = createCommunityUpdateRequest()
                val communityUpdateRequestByteInputStream = jsonContent(communityUpdateRequest).byteInputStream()
                val communityUpdateRequestFile =
                    createCommunityRequestFile(
                        name = TEST_COMMUNITY_UPDATE_REQUEST_NAME,
                        contentStream = communityUpdateRequestByteInputStream,
                    )
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .multipart(targetUri, TEST_COMMUNITY_ID)
                            .file(communityUpdateRequestFile)
                            .file(
                                createMockImageFile(
                                    mockFileName = TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME,
                                    originalFileName = TEST_UPDATE_IMAGE_FILE_WEBP,
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
                            createPathDocument(
                                "community-update-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                                requestParts(
                                    "update-community" requestPartDescription "유효한 커뮤니티 요청 데이터",
                                    "update-community-content-image" requestPartDescription "추가할 커뮤니티 콘텐츠 사진",
                                ),
                            ),
                        )
                }
            }
        }

        describe("DELETE /api/v1/communities/{communityId}") {
            val targetUri = "/api/v1/communities/{communityId}"
            context("유효한 요청 데이터가 전달되면") {
                every { communityService.deleteCommunity(TEST_COMMUNITY_ID, TEST_USER_ID) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNoContent)
                        .andDo(
                            createPathDocument(
                                "community-delete-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }

            context("존재하지 않는 커뮤니티 아이디가 주어지는 경우") {
                every {
                    communityService.deleteCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("404 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isNotFound)
                        .andDo(
                            createPathDocument(
                                "community-delete-fail-not-found",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "존재하지 않는 커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }

            context("삭제 요청자와 작성자가 다른 경우") {
                every {
                    communityService.deleteCommunity(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("요청 사용자($TEST_USER_ID)는 해당 요청을 처리할 권한이 없습니다.")
                it("403 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(status().isForbidden)
                        .andDo(
                            createPathDocument(
                                "community-delete-fail-not-writer",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "작성자가 아닌 사용자의 ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .delete(targetUri, TEST_COMMUNITY_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "community-delete-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                                pathParameters(
                                    "communityId" pathDescription "커뮤니티 아이디" example TEST_COMMUNITY_ID,
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
