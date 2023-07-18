package kr.weit.odya.support.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.odya.service.dto.ErrorResponse
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_INVALID_ID_TOKEN
import kr.weit.odya.support.TEST_BEARER_NOT_EXIST_USER_ID_TOKEN
import kr.weit.odya.support.exception.ErrorCode
import kr.weit.odya.support.userAuthentication
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class TestTokenFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        when (request.getHeader(HttpHeaders.AUTHORIZATION)) {
            null -> {
                filterChain.doFilter(request, response)
            }

            TEST_BEARER_ID_TOKEN -> {
                SecurityContextHolder.getContext().authentication = userAuthentication()
                filterChain.doFilter(request, response)
            }

            TEST_BEARER_NOT_EXIST_USER_ID_TOKEN, TEST_BEARER_INVALID_ID_TOKEN -> {
                response.status = HttpStatus.UNAUTHORIZED.value()
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                jacksonObjectMapper().writeValue(
                    response.outputStream,
                    ErrorResponse.of(ErrorCode.INVALID_FIREBASE_ID_TOKEN, SOMETHING_ERROR_MESSAGE),
                )
            }
        }
    }
}
