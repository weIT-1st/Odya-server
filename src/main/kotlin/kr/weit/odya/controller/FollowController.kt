package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.FollowService
import kr.weit.odya.service.dto.FollowCountsResponse
import kr.weit.odya.service.dto.FollowRequest
import kr.weit.odya.service.dto.FollowUserResponse
import kr.weit.odya.service.dto.PageResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/follows")
class FollowController(
    private val followService: FollowService
) {
    @PostMapping
    fun createFollow(
        @LoginUserId userId: Long,
        @RequestBody @Valid followRequest: FollowRequest
    ): ResponseEntity<Void> {
        followService.createFollow(userId, followRequest)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping
    fun deleteFollow(
        @LoginUserId userId: Long,
        @RequestBody @Valid followRequest: FollowRequest
    ): ResponseEntity<Void> {
        followService.deleteFollow(userId, followRequest)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{userId}/counts")
    fun getFollowCounts(
        @Positive(message = "USER ID는 양수여야 합니다.") @PathVariable("userId") userId: Long
    ): ResponseEntity<FollowCountsResponse> {
        return ResponseEntity.ok(followService.getFollowCounts(userId))
    }

    @GetMapping("/{userId}/followings")
    fun getFollowings(
        @Positive(message = "USER ID는 양수여야 합니다.") @PathVariable("userId") userId: Long,
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable
    ): ResponseEntity<PageResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getFollowings(userId, pageable))
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(
        @Positive(message = "USER ID는 양수여야 합니다.") @PathVariable("userId") userId: Long,
        @PageableDefault(page = 0, size = Int.MAX_VALUE) pageable: Pageable
    ): ResponseEntity<PageResponse<FollowUserResponse>> {
        return ResponseEntity.ok(followService.getFollowers(userId, pageable))
    }
}
