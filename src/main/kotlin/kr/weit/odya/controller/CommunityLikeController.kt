package kr.weit.odya.controller

import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.CommunityLikeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/communities/{communityId}/likes")
class CommunityLikeController(
    private val communityLikeService: CommunityLikeService,
) {
    @PostMapping
    fun createCommunityLike(
        @Positive(message = "communityId는 0보다 커야 합니다.")
        @PathVariable("communityId") communityId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        communityLikeService.createCommunityLike(communityId, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @DeleteMapping
    fun deleteCommunityLike(
        @Positive(message = "communityId는 0보다 커야 합니다.")
        @PathVariable("communityId") communityId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        communityLikeService.deleteCommunityLike(communityId, userId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
