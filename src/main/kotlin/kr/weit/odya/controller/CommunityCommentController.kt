package kr.weit.odya.controller

import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.CommunityCommentService
import kr.weit.odya.service.dto.CommunityCommentRequest
import kr.weit.odya.service.dto.CommunityCommentResponse
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Validated
@RestController
@RequestMapping("/api/v1/communities/{communityId}/comments")
class CommunityCommentController(
    private val communityCommentService: CommunityCommentService,
) {
    @PostMapping
    fun createCommunityComment(
        @Positive(message = "communityId는 1보다 커야합니다.")
        @PathVariable("communityId") communityId: Long,
        @RequestBody communityCommentRequest: CommunityCommentRequest,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        val communityCommentId =
            communityCommentService.createCommunityComment(userId, communityId, communityCommentRequest)
        return ResponseEntity
            .created(URI.create("/api/v1/communities/$communityId/comments/$communityCommentId"))
            .build()
    }

    @GetMapping
    fun getCommunityComments(
        @Positive(message = "communityId는 1보다 커야합니다.")
        @PathVariable("communityId") communityId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<CommunityCommentResponse>> {
        val response = communityCommentService.getCommunityComments(communityId, size, lastId)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{communityCommentId}")
    fun updateCommunityComment(
        @Positive(message = "communityId는 1보다 커야합니다.")
        @PathVariable("communityId") communityId: Long,
        @Positive(message = "communityCommentId는 1보다 커야합니다.")
        @PathVariable("communityCommentId") communityCommentId: Long,
        @RequestBody communityCommentRequest: CommunityCommentRequest,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        communityCommentService.updateCommunityComment(userId, communityId, communityCommentId, communityCommentRequest)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{communityCommentId}")
    fun deleteCommunityComment(
        @Positive(message = "communityId는 1보다 커야합니다.")
        @PathVariable("communityId") communityId: Long,
        @Positive(message = "communityCommentId는 1보다 커야합니다.")
        @PathVariable("communityCommentId") communityCommentId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        communityCommentService.deleteCommunityComment(userId, communityId, communityCommentId)
        return ResponseEntity.noContent().build()
    }
}
