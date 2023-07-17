package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.service.AuthenticationService
import kr.weit.odya.service.LoginFailedException
import kr.weit.odya.service.dto.AppleLoginRequest
import kr.weit.odya.service.dto.AppleRegisterRequest
import kr.weit.odya.service.dto.KakaoLoginRequest
import kr.weit.odya.service.dto.KakaoRegisterRequest
import kr.weit.odya.service.dto.KakaoRegistrationResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authenticationService: AuthenticationService,
) {
    @PostMapping("/login/apple")
    fun appleLogin(
        @RequestBody @Valid
        appleLoginRequest: AppleLoginRequest,
    ): ResponseEntity<Void> {
        val appleUsername = authenticationService.getUsernameByIdToken(appleLoginRequest.idToken)
        authenticationService.appleLoginProcess(appleUsername)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/login/kakao")
    fun kakaoLogin(
        @RequestBody @Valid
        kakaoLoginRequest: KakaoLoginRequest,
    ): ResponseEntity<Any> {
        val kakaoUserInfo = authenticationService.getKakaoUserInfo(kakaoLoginRequest)
        return try {
            val tokenResponse = authenticationService.kakaoLoginProcess(kakaoUserInfo)
            ResponseEntity.ok(tokenResponse)
        } catch (e: LoginFailedException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(KakaoRegistrationResponse(kakaoUserInfo))
        }
    }

    @PostMapping("/register/apple")
    fun appleRegister(
        @RequestBody @Valid
        appleRegisterRequest: AppleRegisterRequest,
    ): ResponseEntity<Void> {
        authenticationService.getUsernameByIdToken(appleRegisterRequest.idToken).apply {
            appleRegisterRequest.updateUsername(this)
        }
        authenticationService.register(appleRegisterRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/register/kakao")
    fun kakaoRegister(
        @RequestBody @Valid
        kakaoRegisterRequest: KakaoRegisterRequest,
    ): ResponseEntity<Void> {
        authenticationService.register(kakaoRegisterRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/validate/nickname")
    fun validateNickname(@RequestParam("value") value: String): ResponseEntity<Void> {
        authenticationService.validateNickname(value)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/validate/email")
    fun validateEmail(@RequestParam("value") value: String): ResponseEntity<Void> {
        authenticationService.validateEmail(value)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/validate/phone-number")
    fun validatePhoneNumber(@RequestParam("value") value: String): ResponseEntity<Void> {
        authenticationService.validatePhoneNumber(value)
        return ResponseEntity.noContent().build()
    }
}
