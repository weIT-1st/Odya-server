package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.ImageService
import kr.weit.odya.service.NotFoundDefaultResourceException
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.service.UserService
import kr.weit.odya.service.WithdrawService
import kr.weit.odya.support.DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_NICKNAME_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.INVALID_DELETE_DEFAULT_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.LAST_ID_PARAM
import kr.weit.odya.support.NICKNAME_PARAM
import kr.weit.odya.support.NOT_ALLOW_FILE_FORMAT_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_ORIGIN_FILE_NAME_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_PROFILE_COLOR_ERROR_MESSAGE
import kr.weit.odya.support.PHONE_NUMBERS_PARAM
import kr.weit.odya.support.SIZE_PARAM
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_LAST_ID
import kr.weit.odya.support.TEST_INVALID_PHONE_NUMBER
import kr.weit.odya.support.TEST_INVALID_SIZE
import kr.weit.odya.support.TEST_INVALID_USER_ID
import kr.weit.odya.support.TEST_LAST_ID
import kr.weit.odya.support.TEST_MOCK_PROFILE_NAME
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_PROFILE_WEBP
import kr.weit.odya.support.TEST_SIZE
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createFcmTokenRequest
import kr.weit.odya.support.createInformationRequest
import kr.weit.odya.support.createMockProfile
import kr.weit.odya.support.createPhoneNumberList
import kr.weit.odya.support.createSimpleUserResponseList
import kr.weit.odya.support.createSliceLifeShotImageResponse
import kr.weit.odya.support.createSliceSimpleUserResponse
import kr.weit.odya.support.createUserResponse
import kr.weit.odya.support.createUserStatisticsResponse
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(UserController::class)
class UserControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val userService: UserService,
    @MockkBean private val withdrawService: WithdrawService,
    @MockkBean private val imageService: ImageService,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("GET /api/v1/users/me") {
            val targetUri = "/api/v1/users/me"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createUserResponse()
                every { userService.getInformation(TEST_USER_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "get-my-information-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "userId" type JsonFieldType.NUMBER description "사용자 ID" example response.userId isOptional true,
                                "email" type JsonFieldType.STRING description "사용자 이메일" example response.email isOptional true,
                                "nickname" type JsonFieldType.STRING description "사용자 닉네임" example response.nickname,
                                "phoneNumber" type JsonFieldType.STRING description "사용자 전화번호" example response.phoneNumber isOptional true,
                                "gender" type JsonFieldType.STRING description "사용자 성별" example response.gender.name,
                                "birthday" type JsonFieldType.STRING description "사용자 생일" example response.birthday,
                                "socialType" type JsonFieldType.STRING description "사용자 소셜 타입" example response.socialType.name,
                                "profile.profileUrl" type JsonFieldType.STRING description "사용자 프로필 Url" example response.profile.profileUrl,
                                "profile.profileColor.colorHex" type JsonFieldType.STRING description "색상 Hex" example response.profile.profileColor?.colorHex isOptional true,
                                "profile.profileColor.red" type JsonFieldType.NUMBER description "RGB RED" example response.profile.profileColor?.red isOptional true,
                                "profile.profileColor.green" type JsonFieldType.NUMBER description "RGB GREEN" example response.profile.profileColor?.green isOptional true,
                                "profile.profileColor.blue" type JsonFieldType.NUMBER description "RGB BLUE" example response.profile.profileColor?.blue isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-my-information-fail-not-registered-user",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 PreAuthentication Access Url 생성에 실패한 경우") {
                every { userService.getInformation(TEST_USER_ID) } throws ObjectStorageException(SOMETHING_ERROR_MESSAGE)
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "get-my-information-fail-create-pre-authentication-access-url",
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
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "get-my-information-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/users/email") {
            val targetUri = "/api/v1/users/email"
            context("유효한 토큰이면서, 가입되지 않은 이메일인 경우") {
                every { userService.getEmailByIdToken(TEST_ID_TOKEN) } returns TEST_EMAIL
                every { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "update-email-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN  WITH VALID EMAIL",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 인증된 이메일이 토큰에 없는 경우") {
                every { userService.getEmailByIdToken(TEST_ID_TOKEN) } throws NoSuchElementException(
                    NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE,
                )
                it("404 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "update-email-fail-not-exist-email-in-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITHOUT AUTHENTICATED EMAIL",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 가입되어 있는 이메일인 경우") {
                every { userService.getEmailByIdToken(TEST_ID_TOKEN) } returns TEST_EMAIL
                every { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) } throws ExistResourceException(
                    EXIST_EMAIL_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "update-email-fail-exist-email",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH EXIST EMAIL",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "update-email-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/users/phone-number") {
            val targetUri = "/api/v1/users/phone-number"
            context("유효한 토큰이면서, 가입되지 않은 전화번호인 경우") {
                every { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) } returns TEST_PHONE_NUMBER
                every { userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "update-phone-number-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH VALID PHONE NUMBER",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 인증된 전화번호가 토큰에 없는 경우") {
                every { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) } throws NoSuchElementException(
                    NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE,
                )
                it("404 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createDocument(
                            "update-phone-number-fail-not-exist-phone-number-in-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITHOUT AUTHENTICATED PHONE NUMBER",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 가입되어 있는 전화번호인 경우") {
                every { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) } returns TEST_PHONE_NUMBER
                every { userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER) } throws ExistResourceException(
                    EXIST_PHONE_NUMBER_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "update-phone-number-fail-exist-email",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH EXIST PHONE NUMBER",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "update-phone-number-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/users/information") {
            val targetUri = "/api/v1/users/information"
            context("유효한 토큰이면서, 유효한 정보인 경우") {
                val informationRequest = createInformationRequest()
                every { userService.updateInformation(TEST_USER_ID, informationRequest) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(informationRequest)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "update-information-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "nickname" type JsonFieldType.STRING description "닉네임" example informationRequest.nickname,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이지만, 중복된 닉네임인 경우") {
                val informationRequest = createInformationRequest()
                every { userService.updateInformation(TEST_USER_ID, informationRequest) } throws ExistResourceException(
                    EXIST_NICKNAME_ERROR_MESSAGE,
                )
                it("409 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(informationRequest)
                    }.andExpect {
                        status { isConflict() }
                    }.andDo {
                        createDocument(
                            "update-information-fail-exist-nickname",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "nickname" type JsonFieldType.STRING description "중복된 닉네임" example informationRequest.nickname,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "update-information-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/users/profile") {
            val targetUri = "/api/v1/users/profile"
            context("유효한 토큰이면서, 프로필 사진을 전송한 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.uploadProfile(mockProfile)
                } returns TEST_GENERATED_FILE_NAME
                every {
                    userService.updateProfile(
                        TEST_USER_ID,
                        TEST_GENERATED_FILE_NAME,
                        TEST_PROFILE_WEBP,
                    )
                } just runs
                it("204 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(mockProfile)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "profile" requestPartDescription "프로필 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 사진을 전송하지 않은 경우") {
                every { userService.deleteProfile(TEST_USER_ID) } just runs
                every { userService.updateProfile(TEST_USER_ID, null, null) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-success-without-profile-image",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 사진의 형식이 잘못된 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.uploadProfile(mockProfile)
                } throws IllegalArgumentException(
                    NOT_ALLOW_FILE_FORMAT_ERROR_MESSAGE,
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(mockProfile)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-invalid-format",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "profile" requestPartDescription "잘못된 형식의 프로필 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 사진의 원본 파일 이름이 존재하지 않는 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.uploadProfile(mockProfile)
                } throws IllegalArgumentException(
                    NOT_EXIST_ORIGIN_FILE_NAME_ERROR_MESSAGE,
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(mockProfile)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-not-exist-origin-file-name",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "profile" requestPartDescription "원본 이름이 없는 프로필 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, Object Storage에 저장하는데 실패한 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.uploadProfile(mockProfile)
                } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("500 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(mockProfile)
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-object-storage-error",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "profile" requestPartDescription "프로필 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 사진을 첨부하지 않고 이미 기본 프로필인 경우") {
                every { userService.deleteProfile(TEST_USER_ID) } throws IllegalArgumentException(
                    INVALID_DELETE_DEFAULT_PROFILE_ERROR_MESSAGE,
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-invalid-delete-default-profile",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, Object Storage에 프로필이 없어 삭제에 실패한 경우") {
                every { userService.deleteProfile(TEST_USER_ID) } throws IllegalArgumentException(
                    DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE,
                )
                it("400 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-delete-not-exist-profile",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 삭제에 실패한 경우") {
                every { userService.deleteProfile(TEST_USER_ID) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("500 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-delete-profile-error",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, DB에 프로필 색상이 없는 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.updateProfile(
                        TEST_USER_ID,
                        TEST_MOCK_PROFILE_NAME,
                        TEST_DEFAULT_PROFILE_PNG,
                    )
                } throws NotFoundDefaultResourceException(NOT_EXIST_PROFILE_COLOR_ERROR_MESSAGE)
                it("500 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        file(mockProfile)
                    }.andExpect {
                        status { isInternalServerError() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-object-storage-error",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "profile" requestPartDescription "프로필 사진" isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                val mockProfile = createMockProfile()
                it("401 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        file(mockProfile)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "update-profile-image-fail-unauthorized",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestParts(
                                "profile" requestPartDescription "프로필 사진" isOptional true,
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/users/fcm-token") {
            val targetUri = "/api/v1/users/fcm-token"
            context("유효한 토큰이면서, 유효한 FCM Token인 경우") {
                val request = createFcmTokenRequest()
                every { userService.updateFcmToken(TEST_USER_ID, request) } just runs
                it("204 응답한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "update-fcm-token-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN WITH VALID PHONE NUMBER",
                            ),
                            requestBody(
                                "fcmToken" type JsonFieldType.STRING description "FCM 토큰" example request.fcmToken,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서 FCM Token이 빈 문자열이면") {
                val request = createFcmTokenRequest(" ")
                it("400를 반환한다.") {
                    restDocMockMvc.post(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "update-fcm-token-failed-invalid-fcm-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "fcmToken" type JsonFieldType.STRING description "유효하지 않은 fcmToken" example " ",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                val request = createFcmTokenRequest()
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "update-fcm-token-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "fcmToken" type JsonFieldType.STRING description "FCM 토큰" example request.fcmToken,
                            ),
                        )
                    }
                }
            }
        }

        describe("DELETE /api/v1/users") {
            val targetUri = "/api/v1/users"
            context("유효한 토큰과 요청이 들어오면,") {
                every { withdrawService.withdrawUser(TEST_USER_ID) } just Runs
                it("204 응답한다.") {
                    restDocMockMvc.delete(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createDocument(
                            "withdraw-user-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("Object Storage에 프로필이 존재하지 않는 경우,") {
                every { withdrawService.withdrawUser(TEST_USER_ID) } throws IllegalArgumentException(DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE)
                it("400 응답한다.") {
                    restDocMockMvc.delete(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "withdraw-user-fail-not-exist-content-image",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면,") {
                it("401 응답한다.") {
                    restDocMockMvc.delete(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "withdraw-user-fail-unauthorized",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/users/search") {
            val targetUri = "/api/v1/users/search"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createSliceSimpleUserResponse()
                every { userService.searchByNickname(TEST_USER_ID, TEST_NICKNAME, TEST_SIZE, TEST_LAST_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        val content = response.content[0]
                        createDocument(
                            "search-users-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example "null" isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                NICKNAME_PARAM parameterDescription "검색할 닉네임" example TEST_NICKNAME,
                            ),
                            responseBody(
                                "hasNext" type JsonFieldType.BOOLEAN description "데이터가 더 존재하는지 여부" example response.hasNext,
                                "content[].userId" type JsonFieldType.NUMBER description "사용자 ID" example content.userId,
                                "content[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example content.nickname,
                                "content[].isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example content.isFollowing,
                                "content[].profile.profileUrl" type JsonFieldType.STRING description "사용자 프로필 Url" example content.profile.profileUrl,
                                "content[].profile.profileColor.colorHex" type JsonFieldType.STRING description "색상 Hex" example content.profile.profileColor?.colorHex isOptional true,
                                "content[].profile.profileColor.red" type JsonFieldType.NUMBER description "RGB RED" example content.profile.profileColor?.red isOptional true,
                                "content[].profile.profileColor.green" type JsonFieldType.NUMBER description "RGB GREEN" example content.profile.profileColor?.green isOptional true,
                                "content[].profile.profileColor.blue" type JsonFieldType.NUMBER description "RGB BLUE" example content.profile.profileColor?.blue isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 닉네임이 올바르지 않은 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, " ")
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-users-fail-nickname-blank",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                NICKNAME_PARAM parameterDescription "올바르지 않은 닉네임" example " ",
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
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-users-fail-invalid-last-id",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
                                NICKNAME_PARAM parameterDescription "검색할 닉네임" example TEST_NICKNAME,
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
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-users-fail-invalid-size",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                                LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                NICKNAME_PARAM parameterDescription "검색할 닉네임" example TEST_NICKNAME,
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
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "search-users-fail-not-registered-user",
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
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        param(SIZE_PARAM, TEST_SIZE.toString())
                        param(NICKNAME_PARAM, TEST_NICKNAME)
                        param(LAST_ID_PARAM, TEST_LAST_ID.toString())
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "search-users-fail-invalid-token",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("GET /api/v1/users/{userId}/statistics") {
            val targetUri = "/api/v1/users/{userId}/statistics"
            context("유효한 토큰이면서, 유효한 요청인 경우") {
                val response = createUserStatisticsResponse()
                every { userService.getStatistics(TEST_USER_ID) } returns response
                it("200 응답한다.") {

                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isOk)
                        .andDo(
                            createPathDocument(
                                "get-user-statistics-success",
                                pathParameters(
                                    "userId" pathDescription "유저 통계를 조회할 USER ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                responseBody(
                                    "travelJournalCount" type JsonFieldType.NUMBER description "여행일지 수" example response.travelJournalCount,
                                    "travelPlaceCount" type JsonFieldType.NUMBER description "여행한 장소 수" example response.travelPlaceCount,
                                    "followingsCount" type JsonFieldType.NUMBER description "팔로잉 수" example response.followingsCount,
                                    "followersCount" type JsonFieldType.NUMBER description "팔로워 수" example response.followersCount,
                                    "odyaCount" type JsonFieldType.NUMBER description "총 오댜수" example response.odyaCount,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, USER ID가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders
                            .get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-user-statistics-fail-user-id-negative",
                                pathParameters(
                                    "userId" pathDescription "음수의 USER ID" example TEST_INVALID_USER_ID,
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
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-user-statistics-fail-not-registered-user",
                                pathParameters(
                                    "userId" pathDescription "유저 통계를 조회할 USER ID" example TEST_USER_ID,
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
                            .get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-user-statistics-fail-invalid-token",
                                pathParameters(
                                    "userId" pathDescription "유저 통계를 조회할 USER ID" example TEST_USER_ID,
                                ),
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/users/{userId}/life-shots") {
            val targetUri = "/api/v1/users/{userId}/life-shots"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createSliceLifeShotImageResponse()
                val content = response.content[0]
                every { imageService.getLifeShots(TEST_USER_ID, TEST_SIZE, TEST_LAST_ID) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    ).andExpect(MockMvcResultMatchers.status().isOk)
                        .andDo(
                            createPathDocument(
                                "get-life-shots-success",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example "null" isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example "null" isOptional true,
                                ),
                                pathParameters(
                                    "userId" pathDescription "USER ID" example TEST_USER_ID,
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
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, userId가 음수인 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_INVALID_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-life-shots-fail-invalid-user-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                ),
                                pathParameters(
                                    "userId" pathDescription "음수인 USER ID" example TEST_INVALID_USER_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 조회할 마지막 ID가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_INVALID_LAST_ID.toString()),
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-life-shots-fail-invalid-last-id",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "츨력할 리스트 사이즈(default=10)" example TEST_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "양수가 아닌 마지막 데이터의 ID" example TEST_INVALID_LAST_ID isOptional true,
                                ),
                                pathParameters(
                                    "userId" pathDescription "USER ID" example TEST_USER_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이지만 size가 양수가 아닌 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_INVALID_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    ).andExpect(MockMvcResultMatchers.status().isBadRequest)
                        .andDo(
                            createPathDocument(
                                "get-life-shots-fail-invalid-size",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                                queryParameters(
                                    SIZE_PARAM parameterDescription "양수가 아닌 데이터 개수" example TEST_INVALID_SIZE isOptional true,
                                    LAST_ID_PARAM parameterDescription "마지막 리스트 ID" example TEST_LAST_ID isOptional true,
                                ),
                                pathParameters(
                                    "userId" pathDescription "USER ID" example TEST_USER_ID,
                                ),
                            ),
                        )
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_USER_ID)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-life-shots-fail-not-registered-user",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }

            context("유효하지 않은 토큰이면") {
                it("401 응답한다.") {
                    restDocMockMvc.perform(
                        RestDocumentationRequestBuilders.get(targetUri, TEST_BEARER_INVALID_ID_TOKEN)
                            .header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                            .param(SIZE_PARAM, TEST_SIZE.toString())
                            .param(LAST_ID_PARAM, TEST_LAST_ID.toString()),
                    )
                        .andExpect(MockMvcResultMatchers.status().isUnauthorized)
                        .andDo(
                            createPathDocument(
                                "get-life-shots-fail-invalid-token",
                                requestHeaders(
                                    HttpHeaders.AUTHORIZATION headerDescription "INVALID ID TOKEN",
                                ),
                            ),
                        )
                }
            }
        }

        describe("GET /api/v1/users/search/phone-number") {
            val targetUri = "/api/v1/users/search/phone-number"
            context("유효한 토큰이면서, 가입된 사용자인 경우") {
                val response = createSimpleUserResponseList()
                val phoneNumbers = createPhoneNumberList()
                every { userService.searchByPhoneNumbers(TEST_USER_ID, phoneNumbers) } returns response
                it("200 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PHONE_NUMBERS_PARAM, TEST_PHONE_NUMBER)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        val content = response[0]
                        createDocument(
                            "search-users-by-phone-number-success",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                PHONE_NUMBERS_PARAM parameterDescription "검색할 전화 번호(최소 1개, 최대 10개)" example TEST_PHONE_NUMBER,
                            ),
                            responseBody(
                                "[].userId" type JsonFieldType.NUMBER description "사용자 ID" example content.userId,
                                "[].nickname" type JsonFieldType.STRING description "사용자 닉네임" example content.nickname,
                                "[].isFollowing" type JsonFieldType.BOOLEAN description "사용자 팔로잉 여부" example content.isFollowing,
                                "[].profile.profileUrl" type JsonFieldType.STRING description "사용자 프로필 Url" example content.profile.profileUrl,
                                "[].profile.profileColor.colorHex" type JsonFieldType.STRING description "색상 Hex" example content.profile.profileColor?.colorHex isOptional true,
                                "[].profile.profileColor.red" type JsonFieldType.NUMBER description "RGB RED" example content.profile.profileColor?.red isOptional true,
                                "[].profile.profileColor.green" type JsonFieldType.NUMBER description "RGB GREEN" example content.profile.profileColor?.green isOptional true,
                                "[].profile.profileColor.blue" type JsonFieldType.NUMBER description "RGB BLUE" example content.profile.profileColor?.blue isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 검색할 전화번호를 보내지 않은 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-users-by-phone-number-fail-empty-phone-number",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 검색할 전화번호를 10개를 넘개 보낸 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        repeat(11) {
                            param(PHONE_NUMBERS_PARAM, TEST_PHONE_NUMBER)
                        }
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-users-by-phone-number-fail-over-10-phone-number",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                PHONE_NUMBERS_PARAM parameterDescription "10개가 넘는 전화번호",
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 올바르지 않은 전화번호를 보낸 경우") {
                it("400 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        param(PHONE_NUMBERS_PARAM, TEST_INVALID_PHONE_NUMBER)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "search-users-by-phone-number-fail-invalid-phone-number",
                            requestHeaders(
                                HttpHeaders.AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            queryParameters(
                                PHONE_NUMBERS_PARAM parameterDescription "유효하지 않은 전화 번호" example TEST_INVALID_PHONE_NUMBER,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 가입되지 않은 사용자인 경우") {
                it("401 응답한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_NOT_EXIST_USER_ID_TOKEN)
                        param(PHONE_NUMBERS_PARAM, TEST_PHONE_NUMBER)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "search-users-by-phone-number-fail-not-registered-user",
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
                        header(HttpHeaders.AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        param(PHONE_NUMBERS_PARAM, TEST_PHONE_NUMBER)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "search-users-by-phone-number-fail-invalid-token",
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
