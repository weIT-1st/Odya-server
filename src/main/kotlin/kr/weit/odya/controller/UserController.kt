package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.UserService
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.UserResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/me")
    fun getMyInfo(@LoginUserId userId: Long): ResponseEntity<UserResponse> {
        val response = userService.getInformation(userId)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/email")
    fun updateEmail(
        @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        val idToken = bearerToken.split(" ")[1]
        val email = userService.getEmailByIdToken(idToken)
        userService.updateEmail(userId, email)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/phone-number")
    fun updatePhoneNumber(
        @RequestHeader(HttpHeaders.AUTHORIZATION) bearerToken: String,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        val idToken = bearerToken.split(" ")[1]
        val phoneNumber = userService.getPhoneNumberByIdToken(idToken)
        userService.updatePhoneNumber(userId, phoneNumber)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/information")
    fun updateInformation(
            @RequestBody
            @Valid
            informationRequest: InformationRequest,
            @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        userService.updateInformation(userId, informationRequest)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/profile")
    fun updateProfile(
        @RequestPart(name = "profile", required = false) multipartFile: MultipartFile?,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        val profileName: String? = if (multipartFile != null) {
            userService.uploadProfile(multipartFile.inputStream, multipartFile.originalFilename)
        } else {
            userService.deleteProfile(userId)
            null
        }
        userService.updateProfile(userId, profileName, multipartFile?.originalFilename)
        return ResponseEntity.noContent().build()
    }
}
