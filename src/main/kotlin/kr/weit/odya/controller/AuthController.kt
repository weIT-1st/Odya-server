package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.service.AuthenticationService
import kr.weit.odya.service.dto.LoginRequest
import kr.weit.odya.service.dto.RegisterRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService
) {
    @PostMapping("/login")
    fun login(@RequestBody @Valid loginRequest: LoginRequest): ResponseEntity<Void> {
        authenticationService.loginProcess(loginRequest)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/register/{provider}")
    fun register(
        @RequestBody @Valid registerRequest: RegisterRequest,
        @PathVariable("provider") provider: String
    ): ResponseEntity<Void> {
        authenticationService.register(registerRequest, provider)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/validate/nickname")
    fun validateNickname(@RequestParam("value") value: String): ResponseEntity<Void> {
        authenticationService.validateNickname(value)
        return ResponseEntity.noContent().build()
    }
}
