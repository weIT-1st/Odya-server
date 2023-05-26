package kr.weit.odya.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

data class ApiErrorResponse(val errorMessage: String?)

@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        logger.error("[IllegalArgumentException]", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiErrorResponse(ex.message))
    }
}
