package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.UserService
import kr.weit.odya.service.WithdrawService
import kr.weit.odya.service.dto.FCMTokenRequest
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.UserResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import kr.weit.odya.service.dto.UserStatisticsResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val withdrawService: WithdrawService,
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
            userService.uploadProfile(multipartFile)
        } else {
            userService.deleteProfile(userId)
            null
        }
        userService.updateProfile(userId, profileName, multipartFile?.originalFilename)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/fcm-token")
    fun updateFcmToken(
        @RequestBody
        @Valid
        fcmTokenRequest: FCMTokenRequest,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        userService.updateFcmToken(userId, fcmTokenRequest)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    fun search(
        @NotBlank(message = "검색할 닉네임은 필수입니다.")
        @RequestParam("nickname")
        nickname: String,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<UserSimpleResponse>> {
        return ResponseEntity.ok(userService.searchByNickname(nickname, size, lastId))
    }

    @GetMapping("/{userId}/statistics")
    fun getStatistics(
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
    ): ResponseEntity<UserStatisticsResponse> {
        val response = userService.getStatistics(userId)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    fun withdrawUser(
        @LoginUserId
        userId: Long,
    ): ResponseEntity<Void> {
        withdrawService.withdrawUser(userId)
        return ResponseEntity.noContent().build()
    }
}
