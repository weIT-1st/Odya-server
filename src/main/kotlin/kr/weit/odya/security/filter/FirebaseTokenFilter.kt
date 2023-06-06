package kr.weit.odya.security.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.odya.security.FirebaseTokenParser
import kr.weit.odya.security.UserDetailsService
import kr.weit.odya.support.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

val FIREBASE_TOKEN_FILTER_PERMITTED_PATTERNS: List<String> = listOf("/api/v1/auth/**", "/test", "/ready", "/health")

data class TokenInvalidErrorResponse(val errorMessage: String)

private const val BEARER = "Bearer "
private const val TOKEN_INVALID_ERROR_MESSAGE = "TOKEN INVALID"

class FirebaseTokenFilter(
    private val userDetailsService: UserDetailsService,
    private val firebaseTokenParser: FirebaseTokenParser
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (!isPermittedRequest(request)) {
            try {
                val firebaseToken = request.getHeader(HttpHeaders.AUTHORIZATION)
                val idToken = getIdTokenByFirebaseToken(firebaseToken)
                val username = firebaseTokenParser.getUsername(idToken)
                val userDetails = userDetailsService.loadUserByUsername(username)
                val authenticationToken =
                    UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                SecurityContextHolder.getContext().authentication = authenticationToken
            } catch (ex: IllegalArgumentException) {
                setErrorResponse(response, ex)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun setErrorResponse(response: HttpServletResponse, ex: IllegalArgumentException) {
        Logger.error(ex) { "[FirebaseTokenFilter] ${ex.message}" }
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        jacksonObjectMapper().writeValue(
            response.outputStream,
            TokenInvalidErrorResponse(ex.message ?: TOKEN_INVALID_ERROR_MESSAGE)
        )
    }

    private fun isPermittedRequest(request: HttpServletRequest): Boolean {
        return FIREBASE_TOKEN_FILTER_PERMITTED_PATTERNS.any { patterns ->
            request.requestURI.startsWith(patterns.removeSuffix("/**"))
        }
    }

    private fun getIdTokenByFirebaseToken(firebaseToken: String): String {
        if (!firebaseToken.startsWith(BEARER)) {
            throw IllegalArgumentException()
        }
        return firebaseToken.split(" ")[1]
    }
}
