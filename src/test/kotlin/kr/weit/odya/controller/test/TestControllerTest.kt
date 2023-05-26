package kr.weit.odya.controller.test

import kr.weit.odya.controller.TestController
import kr.weit.odya.support.createTestEmptyRequest
import kr.weit.odya.support.createTestErrorRequest
import kr.weit.odya.support.createTestRequest
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.post
import support.test.ControllerTestHelper
import support.test.EXAMPLE

@WebMvcTest(TestController::class)
class TestControllerTest : ControllerTestHelper() {
    @Test
    fun `테스트 API - 성공`() {
        val request = createTestRequest()

        mockMvc.post("/test") {
            jsonContent(request)
        }.andExpect {
            status { isOk() }
        }.andDo {
            handle(
                MockMvcRestDocumentation.document(
                    "{class-name}/test-success",
                    requestFields(
                        fieldWithPath("name").description("이름").attributes(field(EXAMPLE, request.name))
                    ),
                    responseFields(
                        fieldWithPath("hashValue").description("name의 hashCode 값"),
                        fieldWithPath("originalName").description("trim을 적용한 name 값")
                    )
                )
            )
        }
    }

    @Test
    fun `테스트 API - 실패 (name is empty)`() {
        val request = createTestEmptyRequest()

        mockMvc.post("/test") {
            jsonContent(request)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            handle(
                MockMvcRestDocumentation.document(
                    "{class-name}/test-fail-empty-name",
                    requestFields(
                        fieldWithPath("name").description("빈 값").attributes(field(EXAMPLE, request.name))
                    ),
                    responseFields(
                        fieldWithPath("errorMessage").description("에러 메시지")
                    )
                )
            )
        }
    }

    @Test
    fun `테스트 API - 실패 (name is 김한빈)`() {
        val request = createTestErrorRequest()

        mockMvc.post("/test") {
            jsonContent(request)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            handle(
                MockMvcRestDocumentation.document(
                    "{class-name}/test-fail-error-name",
                    requestFields(
                        fieldWithPath("name").description("에러 발생 이름").attributes(field(EXAMPLE, request.name))
                    ),
                    responseFields(
                        fieldWithPath("errorMessage").description("에러 메시지")
                    )
                )
            )
        }
    }
}
