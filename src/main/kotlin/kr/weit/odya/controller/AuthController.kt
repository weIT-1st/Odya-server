package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.service.AuthenticationService
import kr.weit.odya.service.TermsService
import kr.weit.odya.service.UnRegisteredUserException
import kr.weit.odya.service.dto.AppleLoginRequest
import kr.weit.odya.service.dto.AppleRegisterRequest
import kr.weit.odya.service.dto.KakaoLoginRequest
import kr.weit.odya.service.dto.KakaoRegisterErrorResponse
import kr.weit.odya.service.dto.KakaoRegisterRequest
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
    private val termsService: TermsService,
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
        } catch (e: UnRegisteredUserException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(KakaoRegisterErrorResponse(kakaoUserInfo))
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
        val termsIdList = appleRegisterRequest.termsIdList
        termsService.checkRequiredTerms(termsIdList)
        val user = authenticationService.register(appleRegisterRequest)
        termsService.saveAllAgreedTerms(user, termsIdList)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/register/kakao")
    fun kakaoRegister(
        @RequestBody @Valid
        kakaoRegisterRequest: KakaoRegisterRequest,
    ): ResponseEntity<Void> {
        val termsIdList = kakaoRegisterRequest.termsIdList
        termsService.checkRequiredTerms(termsIdList)
        val user = authenticationService.register(kakaoRegisterRequest)
        termsService.saveAllAgreedTerms(user, termsIdList)
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
