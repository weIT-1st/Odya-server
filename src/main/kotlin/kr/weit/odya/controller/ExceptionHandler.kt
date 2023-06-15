package kr.weit.odya.controller

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.service.ExistResourceException
import kr.weit.odya.service.LoginFailedException
import kr.weit.odya.service.OdyaException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ApiErrorResponse(val errorMessage: String?)

@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[MethodArgumentNotValidException] ${ex.messages()}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.messages().joinToString(" ")))
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[HttpMessageNotReadableException] ${ex.message}")
        val errorMessage = when (val cause = ex.cause) {
            is MismatchedInputException -> "${cause.path.joinToString { it.fieldName }}: ${ex.message}"
            else -> "유효하지 않은 요청입니다"
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(errorMessage))
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[HttpRequestMethodNotSupportedException] ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun illegalException(ex: RuntimeException): ResponseEntity<ApiErrorResponse> {
        logger.error("[IllegalException]", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun constraintViolationException(ex: ConstraintViolationException): ResponseEntity<ApiErrorResponse> {
        logger.error("[ConstraintViolationException]", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun noSuchElementException(ex: NoSuchElementException): ResponseEntity<ApiErrorResponse> {
        logger.error("[NoSuchElementException]", ex)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(InvalidTokenException::class, LoginFailedException::class)
    fun loginFailedException(ex: RuntimeException): ResponseEntity<ApiErrorResponse> {
        logger.error("[UnauthorizedException]", ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(ExistResourceException::class)
    fun existResourceException(ex: OdyaException): ResponseEntity<ApiErrorResponse> {
        logger.error("[ExistResourceException]", ex)
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseEntity<ApiErrorResponse> {
        logger.error("[Exception]", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiErrorResponse(ex.message))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun forbiddenException(ex: ForbiddenException): ResponseEntity<ApiErrorResponse> {
        logger.error("[ForbiddenException]", ex)
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse(ex.message))
    }

    private fun MethodArgumentNotValidException.messages(): List<String> {
        return bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage.orEmpty()}" }
    }
}
