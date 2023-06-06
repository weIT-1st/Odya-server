package kr.weit.odya.config.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.weit.odya.support.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

private const val UNAUTHORIZED_ERROR_MESSAGE = "UNAUTHORIZED REQUEST"

@Component
class CustomAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        Logger.error(authException) { "[AuthenticationEntryPoint] ${authException?.message}" }
        response?.status = HttpStatus.UNAUTHORIZED.value()
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        jacksonObjectMapper().writeValue(response?.outputStream, UnAuthorizedResponse())
    }
}

data class UnAuthorizedResponse(val errorMessage: String = UNAUTHORIZED_ERROR_MESSAGE)
