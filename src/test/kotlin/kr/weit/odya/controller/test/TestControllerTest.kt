package kr.weit.odya.controller.test

import io.kotest.core.spec.style.DescribeSpec
import kr.weit.odya.controller.TestController
import kr.weit.odya.support.createTestEmptyRequest
import kr.weit.odya.support.createTestErrorRequest
import kr.weit.odya.support.createTestRequest
import kr.weit.odya.support.test.BaseTests.UnitControllerTestEnvironment
import kr.weit.odya.support.test.ControllerTestHelper.Companion.jsonContent
import kr.weit.odya.support.test.RestDocsHelper.Companion.createDocument
import kr.weit.odya.support.test.RestDocsHelper.Companion.generateRestDocMockMvc
import kr.weit.odya.support.test.RestDocsHelper.Companion.requestBody
import kr.weit.odya.support.test.RestDocsHelper.Companion.responseBody
import kr.weit.odya.support.test.type
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.test.web.servlet.post
import org.springframework.web.context.WebApplicationContext

@UnitControllerTestEnvironment
@WebMvcTest(TestController::class)
class TestControllerTest(
    private val context: WebApplicationContext,
) : DescribeSpec(
    {
        val restDocumentation = ManualRestDocumentation()
        val restDocMockMvc = generateRestDocMockMvc(context, restDocumentation)

        beforeEach {
            restDocumentation.beforeTest(javaClass, it.name.testName)
        }

        describe("POST /test") {
            val targetUri = "/test"
            context("유효한 요청 데이터가 전달되면") {
                val request = createTestRequest()
                it("trim을 적용하고 해당 값과 hashCode를 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isOk() }
                    }.andDo {
                        createDocument(
                            "test-success",
                            requestBody("name" type JsonFieldType.STRING description "이름" example request.name),
                            responseBody(
                                "hashValue" type JsonFieldType.NUMBER description "name의 hashCode 값",
                                "originalName" type JsonFieldType.STRING description "trim을 적용한 name 값",
                            ),
                        )
                    }
                }
            }

            context("빈 값의 요청 데이터가 전달되면") {
                val request = createTestEmptyRequest()
                it("이름이 비어있으면 안됨 에러 메시지를 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "test-fail-empty-name",
                            requestBody("name" type JsonFieldType.STRING description "빈 값" example request.name),
                            responseBody("errorMessage" type JsonFieldType.STRING description "에러 메시지"),
                        )
                    }
                }
            }

            context("김한빈이라는 요청 데이터가 전달되면") {
                val request = createTestErrorRequest()
                it("김한빈은 안됨 에러 메시지를 응답한다.") {
                    restDocMockMvc.post(targetUri) {
                        jsonContent(request)
                    }.andExpect {
                        status { isBadRequest() }
                    }.andDo {
                        createDocument(
                            "test-fail-error-name",
                            requestBody("name" type JsonFieldType.STRING description "에러 발생 이름" example request.name),
                            responseBody("errorMessage" type JsonFieldType.STRING description "에러 메시지"),
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
