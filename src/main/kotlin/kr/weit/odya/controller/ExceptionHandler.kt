package kr.weit.odya.controller

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import jakarta.validation.ConstraintViolationException
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.client.ClientException
import kr.weit.odya.security.FirebaseAuthException
import kr.weit.odya.service.OdyaException
import kr.weit.odya.service.dto.ErrorResponse
import kr.weit.odya.support.exception.ErrorCode
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

@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        logger.error("[MethodArgumentNotValidException] ${ex.messages()}")
        return getInvalidRequestResponse(ex.messages().joinToString())
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        logger.error("[HttpMessageNotReadableException] ${ex.message}")
        val errorMessage = when (val cause = ex.cause) {
            is MismatchedInputException -> "${cause.path.joinToString { it.fieldName }}: ${ex.message}"
            else -> "유효하지 않은 요청입니다"
        }
        return getInvalidRequestResponse(errorMessage)
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        logger.error("[HttpRequestMethodNotSupportedException] ${ex.message}")
        return getInvalidRequestResponse(ex.message)
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        IllegalStateException::class,
        ConstraintViolationException::class,
    )
    fun invalidRequestException(ex: RuntimeException): ResponseEntity<Any>? {
        logger.error("[InvalidRequestException]", ex)
        return getInvalidRequestResponse(ex.message)
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun noSuchElementException(ex: NoSuchElementException): ResponseEntity<ErrorResponse> {
        logger.error("[NoSuchElementException]", ex)
        val noSuchElementErrorCode = ErrorCode.NO_SUCH_ELEMENT
        return ResponseEntity.status(noSuchElementErrorCode.httpStatus)
            .body(ErrorResponse.of(noSuchElementErrorCode, ex.message))
    }

    @ExceptionHandler(FirebaseAuthException::class)
    fun firebaseAuthException(ex: FirebaseAuthException): ResponseEntity<ErrorResponse> {
        logger.error("[FirebaseAuthException]", ex)
        return ResponseEntity.status(ex.errorCode.httpStatus).body(ErrorResponse.of(ex.errorCode, ex.message))
    }

    @ExceptionHandler(OdyaException::class)
    fun odyaException(ex: OdyaException): ResponseEntity<ErrorResponse> {
        logger.error("[OdyaException]", ex)
        return ResponseEntity.status(ex.errorCode.httpStatus).body(ErrorResponse.of(ex.errorCode, ex.message))
    }

    @ExceptionHandler(ClientException::class)
    fun odyaException(ex: ClientException): ResponseEntity<ErrorResponse> {
        logger.error("[ClientException]", ex)
        return ResponseEntity.status(ex.errorCode.httpStatus).body(ErrorResponse.of(ex.errorCode, ex.message))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun forbiddenException(ex: ForbiddenException): ResponseEntity<ErrorResponse> {
        logger.error("[ForbiddenException]", ex)
        val forbiddenErrorCode = ErrorCode.FORBIDDEN
        return ResponseEntity.status(forbiddenErrorCode.httpStatus)
            .body(ErrorResponse.of(forbiddenErrorCode, ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("[Exception]", ex)
        val internalServerErrorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(internalServerErrorCode.httpStatus)
            .body(ErrorResponse.of(internalServerErrorCode, ex.message))
    }

    private fun MethodArgumentNotValidException.messages(): List<String> {
        return bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage.orEmpty()}" }
    }

    private fun getInvalidRequestResponse(errorMessage: String?): ResponseEntity<Any>? {
        val invalidRequestErrorCode = ErrorCode.INVALID_REQUEST
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(invalidRequestErrorCode, errorMessage))
    }
}
