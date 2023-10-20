package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.weit.odya.domain.community.CommunitySortType
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.CommunityService
import kr.weit.odya.service.dto.CommunityCreateRequest
import kr.weit.odya.service.dto.CommunityResponse
import kr.weit.odya.service.dto.CommunitySimpleResponse
import kr.weit.odya.service.dto.CommunitySummaryResponse
import kr.weit.odya.service.dto.CommunityUpdateRequest
import kr.weit.odya.service.dto.CommunityWithCommentsResponse
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Validated
@RestController
@RequestMapping("/api/v1/communities")
class CommunityController(
    private val communityService: CommunityService,
) {
    @PostMapping
    fun createCommunity(
        @LoginUserId userId: Long,
        @Valid
        @RequestPart("community")
        communityRequest: CommunityCreateRequest,
        @Size(min = 1, max = 15, message = "이미지는 최소 1개, 최대 15개까지 업로드할 수 있습니다.")
        @RequestPart("community-content-image")
        contentImages: List<MultipartFile>,
    ): ResponseEntity<Void> {
        val contentImagePairs = communityService.uploadContentImages(contentImages)
        val createdCommunityId = communityService.createCommunity(userId, communityRequest, contentImagePairs)
        return ResponseEntity.created(URI.create("/api/v1/communities/$createdCommunityId")).build()
    }

    @GetMapping("/{communityId}")
    fun getCommunity(
        @Positive(message = "커뮤니티 아이디는 0보다 커야 합니다.")
        @PathVariable("communityId")
        communityId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<CommunityResponse> {
        val response = communityService.getCommunity(communityId, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getCommunities(
        @LoginUserId userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: CommunitySortType,
    ): ResponseEntity<SliceResponse<CommunitySummaryResponse>> {
        val response = communityService.getCommunities(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMyCommunities(
        @LoginUserId userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: CommunitySortType,
    ): ResponseEntity<SliceResponse<CommunitySimpleResponse>> {
        val response = communityService.getMyCommunities(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/friends")
    fun getFriendCommunities(
        @LoginUserId userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: CommunitySortType,
    ): ResponseEntity<SliceResponse<CommunitySummaryResponse>> {
        val response = communityService.getFriendCommunities(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/topic/{topicId}")
    fun searchByTopic(
        @LoginUserId
        userId: Long,
        @Positive(message = "조회할 토픽ID는 양수여야 합니다.")
        @PathVariable("topicId")
        topicId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: CommunitySortType,
    ): ResponseEntity<SliceResponse<CommunitySummaryResponse>> {
        val response = communityService.searchByTopic(userId, topicId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/like")
    fun likedCommunities(
        @LoginUserId
        userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: CommunitySortType,
    ): ResponseEntity<SliceResponse<CommunitySimpleResponse>> {
        val response = communityService.getLikedCommunities(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/comment")
    fun communityWithComments(
        @LoginUserId
        userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: CommunitySortType,
    ): ResponseEntity<SliceResponse<CommunityWithCommentsResponse>> {
        val response = communityService.getCommunityWithComments(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{communityId}")
    fun updateCommunity(
        @Positive(message = "커뮤니티 아이디는 0보다 커야 합니다.")
        @PathVariable("communityId")
        communityId: Long,
        @RequestPart("update-community") communityUpdateRequest: CommunityUpdateRequest,
        @RequestPart("update-community-content-image", required = false) images: List<MultipartFile>?,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        val updateImageSize = images?.size ?: 0
        communityService.validateUpdateCommunityRequest(
            communityId,
            userId,
            communityUpdateRequest.deleteCommunityContentImageIds,
            updateImageSize,
        )
        val contentImagePairs = communityService.uploadContentImages(images ?: emptyList())
        communityService.updateCommunity(communityId, userId, communityUpdateRequest, contentImagePairs)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{communityId}")
    fun deleteCommunity(
        @Positive(message = "커뮤니티 아이디는 0보다 커야 합니다.")
        @PathVariable("communityId")
        communityId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        communityService.deleteCommunity(communityId, userId)
        return ResponseEntity.noContent().build()
    }
}
