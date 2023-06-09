package kr.weit.odya.support.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.odya.support.TEST_BEARER_ID_TOKEN
import kr.weit.odya.support.userAuthentication
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class TestTokenFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val loginToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (loginToken == TEST_BEARER_ID_TOKEN) {
            SecurityContextHolder.getContext().authentication = userAuthentication()
        }
        filterChain.doFilter(request, response)
    }
}
