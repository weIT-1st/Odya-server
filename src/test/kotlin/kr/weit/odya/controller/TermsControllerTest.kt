package kr.weit.odya.controller

import com.google.auth.http.AuthHttpConstants.AUTHORIZATION
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import kr.weit.odya.service.TermsService
import kr.weit.odya.support.INVALID_DELETE_REQUIRED_TERMS_ERROR_MESSAGE
import kr.weit.odya.support.NOT_FOUND_TERMS_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_NOT_EXIST_TERMS_ID
import kr.weit.odya.support.TEST_OTHER_TERMS_ID
import kr.weit.odya.support.TEST_TERMS_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createTermsUpdateRequest
import kr.weit.odya.support.createTermsUpdateResponse
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.createPathDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.headerDescription
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(TermsController::class)
class TermsControllerTest(
    @MockkBean
    private val termsService: TermsService,
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = RestDocsHelper.generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("GET /api/v1/terms") {
            val targetUri = "/api/v1/terms"
            val user = createUser()
            context("유효한 요청이 전달되면") {
                val response = createTermsUpdateResponse(user)
                val optionalAgreedTerms = response.optionalAgreedTermsList[0]
                val userOptionalAgreedTerms = response.userOptionalAgreedTermsList[0]
                every { termsService.getOptionalTermsListAndOptionalAgreedTerms(TEST_USER_ID) } returns response
                it("선택 약관 리스트와 유저가 동의한 선택 약관 리스트 및 200을 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "optional-terms-and-user-agreed-terms-list-get-success",
                            requestHeaders(
                                AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            responseBody(
                                "optionalAgreedTermsList[]" type JsonFieldType.ARRAY description "선택 약관 리스트",
                                "optionalAgreedTermsList[].id" type JsonFieldType.NUMBER description "약관 id" example optionalAgreedTerms.id,
                                "optionalAgreedTermsList[].title" type JsonFieldType.STRING description "약관 제목" example optionalAgreedTerms.title,
                                "optionalAgreedTermsList[].required" type JsonFieldType.NUMBER description "필수 약관 여부(false:0)" example optionalAgreedTerms.required,
                                "optionalAgreedTermsList[]" type JsonFieldType.ARRAY description "유저가 동의한 약관 리스트" example optionalAgreedTerms,
                                "userOptionalAgreedTermsList[].id" type JsonFieldType.NUMBER description "동의한 약관 ID" example userOptionalAgreedTerms.id,
                                "userOptionalAgreedTermsList[].userId" type JsonFieldType.NUMBER description "유저 Id" example userOptionalAgreedTerms.userId,
                                "userOptionalAgreedTermsList[].termsId" type JsonFieldType.NUMBER description "약관 id" example userOptionalAgreedTerms.termsId,
                                "userOptionalAgreedTermsList[].required" type JsonFieldType.NUMBER description "필수 약관의 필수 여부(false:0)" example userOptionalAgreedTerms.required,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰 전달되면") {
                it("401를 반환한다.") {
                    restDocMockMvc.get(targetUri) {
                        header(AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createDocument(
                            "optional-terms-and-user-agreed-terms-list-get-fail-invalid-token",
                            requestHeaders(
                                AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                        )
                    }
                }
            }
        }

        describe("PATCH /api/v1/terms") {
            val targetUri = "/api/v1/terms"
            context("동의 약관 ID 리스트(미동의->동의)와 미동의 약관 ID 리스트(동의->미동의)와 유저 ID가 전달되면") {
                val request = createTermsUpdateRequest()
                every { termsService.modifyAgreedTerms(request, TEST_USER_ID) } just Runs
                it("204를 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNoContent() }
                    }.andDo {
                        createPathDocument(
                            "modify-agreed-terms-success",
                            requestHeaders(
                                AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "agreedTermsIdList" type JsonFieldType.ARRAY description "동의로 변경하는 약관 ID 리스트" example request.agreedTermsIdList isOptional true,
                                "disagreedTermsIdList" type JsonFieldType.ARRAY description "미동의로 변경하는 약관 ID 리스트" example request.disagreeTermsIdList isOptional true,
                            ),
                        )
                    }
                }
            }

            context("존재하지 않는 약관 ID가 동의 약관 ID 리스트(미동의->동의)로 전달되면") {
                val request = createTermsUpdateRequest().copy(agreedTermsIdList = setOf(TEST_OTHER_TERMS_ID, TEST_NOT_EXIST_TERMS_ID))
                every { termsService.modifyAgreedTerms(request, TEST_USER_ID) } throws NoSuchElementException(NOT_FOUND_TERMS_ERROR_MESSAGE)
                it("404를 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isNotFound() }
                    }.andDo {
                        createPathDocument(
                            "modify-agreed-terms-fail-not-found-terms",
                            requestHeaders(
                                AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "agreedTermsIdList" type JsonFieldType.ARRAY description "존재하지 않는 약관 ID가 포함된 동의로 변경하는 약관 ID 리스트" example request.agreedTermsIdList isOptional true,
                                "disagreedTermsIdList" type JsonFieldType.ARRAY description "미동의로 변경하는 약관 ID 리스트" example request.disagreeTermsIdList isOptional true,
                            ),
                        )
                    }
                }
            }

            context("필수 약관 ID가 미동의 약관 ID 리스트(미동의->동의)로 전달되면") {
                val request = createTermsUpdateRequest().copy(disagreeTermsIdList = setOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID))
                every { termsService.modifyAgreedTerms(request, TEST_USER_ID) } throws IllegalArgumentException(INVALID_DELETE_REQUIRED_TERMS_ERROR_MESSAGE)
                it("400을 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(AUTHORIZATION, TEST_BEARER_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createPathDocument(
                            "modify-agreed-terms-fail-invalid-delete-required-terms",
                            requestHeaders(
                                AUTHORIZATION headerDescription "VALID ID TOKEN",
                            ),
                            requestBody(
                                "agreedTermsIdList" type JsonFieldType.ARRAY description "동의로 변경하는 약관 ID 리스트" example request.agreedTermsIdList isOptional true,
                                "disagreedTermsIdList" type JsonFieldType.ARRAY description "필수 약관 ID가 포함된 미동의로 변경하는 약관 ID 리스트" example request.disagreeTermsIdList isOptional true,
                            ),
                        )
                    }
                }
            }

            context("유효하지 않은 토큰이 전달되면") {
                val request = createTermsUpdateRequest()
                it("401를 반환한다.") {
                    restDocMockMvc.patch(targetUri) {
                        header(AUTHORIZATION, TEST_BEARER_INVALID_ID_TOKEN)
                        jsonContent(request)
                    }.andExpect {
                        status { isUnauthorized() }
                    }.andDo {
                        createPathDocument(
                            "modify-agreed-terms-fail-invalid-token",
                            requestHeaders(
                                AUTHORIZATION headerDescription "INVALID ID TOKEN",
                            ),
                            requestBody(
                                "agreedTermsIdList" type JsonFieldType.ARRAY description "동의로 변경하는 약관 ID 리스트" example request.agreedTermsIdList isOptional true,
                                "disagreedTermsIdList" type JsonFieldType.ARRAY description "미동의로 변경하는 약관 ID 리스트" example request.disagreeTermsIdList isOptional true,
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
