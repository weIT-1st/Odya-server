package kr.weit.odya.support.test

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

class ControllerTestHelper {
    companion object {
        private val objectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }

        fun MockHttpServletRequestDsl.jsonContent(value: Any) {
            content = objectToString(value)
            contentType = MediaType.APPLICATION_JSON
        }

        fun MockHttpServletRequestBuilder.jsonContent(value: Any): MockHttpServletRequestBuilder = apply {
            content(objectToString(value))
            contentType(MediaType.APPLICATION_JSON)
        }

        fun jsonContent(value: Any): String = objectToString(value)

        private fun objectToString(value: Any): String = objectMapper.writeValueAsString(value)
    }
}
