package kr.weit.odya.support.log

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

@Component
class TraceFilter(
    private val traceManager: TraceManager,
) : OncePerRequestFilter(), Ordered {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        with(traceManager) {
            this.wrappedRequest = wrappedRequest
            this.wrappedResponse = wrappedResponse
        }

        filterChain.doFilter(wrappedRequest, wrappedResponse)
        wrappedResponse.copyBodyToResponse()
    }

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE - 11 // precedence HttpTraceFilter
    }
}
