package kr.weit.odya.controller

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.NotFoundDefaultResourceException
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.service.UserService
import kr.weit.odya.support.DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_NICKNAME_ERROR_MESSAGE
import kr.weit.odya.support.EXIST_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.INVALID_DELETE_DEFAULT_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.NOT_ALLOW_FILE_FORMAT_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_ORIGIN_FILE_NAME_ERROR_MESSAGE
import kr.weit.odya.support.NOT_EXIST_PROFILE_COLOR_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_MOCK_PROFILE_NAME
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createInformationRequest
import kr.weit.odya.support.createMockProfile
import kr.weit.odya.support.createUserResponse
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.isOptional
import kr.weit.odya.support.test.requestPartDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.patch
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(UserController::class)
class UserControllerTest(
    private val context: WebApplicationContext,
    @MockkBean private val userService: UserService,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example NOT_EXIST_AUTHENTICATED_EMAIL_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example NOT_EXIST_AUTHENTICATED_PHONE_NUMBER_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                    userService.uploadProfile(
                        any(),
                        mockProfile.originalFilename,
                    )
                } returns TEST_MOCK_PROFILE_NAME
                every {
                    userService.updateProfile(
                        TEST_USER_ID,
                        TEST_MOCK_PROFILE_NAME,
                        TEST_DEFAULT_PROFILE_PNG,
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
                    userService.uploadProfile(
                        any(),
                        mockProfile.originalFilename,
                    )
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example NOT_ALLOW_FILE_FORMAT_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, 프로필 사진의 원본 파일 이름이 존재하지 않는 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.uploadProfile(
                        any(),
                        mockProfile.originalFilename,
                    )
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example NOT_EXIST_ORIGIN_FILE_NAME_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }

            context("유효한 토큰이면서, Object Storage에 저장하는데 실패한 경우") {
                val mockProfile = createMockProfile()
                every {
                    userService.uploadProfile(
                        any(),
                        mockProfile.originalFilename,
                    )
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example INVALID_DELETE_DEFAULT_PROFILE_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example NOT_EXIST_PROFILE_COLOR_ERROR_MESSAGE,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이면") {
                val mockProfile = createMockProfile()
                it("401 응답한다.") {
                    restDocMockMvc.multipart(HttpMethod.PATCH, targetUri) {
                        header(HttpHeaders.AUTHORIZATION, TEST_INVALID_ID_TOKEN)
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
                            responseBody(
                                "errorMessage" type JsonFieldType.STRING description "에러 메시지" example SOMETHING_ERROR_MESSAGE,
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
