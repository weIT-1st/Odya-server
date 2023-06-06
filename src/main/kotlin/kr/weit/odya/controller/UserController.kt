package kr.weit.odya.controller

import kr.weit.odya.security.LoginUsername
import kr.weit.odya.service.UserService
import kr.weit.odya.service.dto.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/me")
    fun getMyInfo(@LoginUsername username: String): ResponseEntity<UserResponse> {
        val response = userService.getInformation(username)
        return ResponseEntity.ok(response)
    }
}
