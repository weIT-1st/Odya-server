package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.CommunityService
import kr.weit.odya.service.dto.CommunityCreateRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("/api/v1/communities")
class CommunityController(
    private val communityService: CommunityService,
) {
    @PostMapping
    fun createCommunity(
        @LoginUserId userId: Long,
        @Valid @RequestPart("community") communityRequest: CommunityCreateRequest,
        @Size(min = 1, max = 15, message = "이미지는 최소 1개, 최대 15개까지 업로드할 수 있습니다.")
        @RequestPart("community-content-image") contentImages: List<MultipartFile>,
    ): ResponseEntity<Void> {
        val contentImagePairs = communityService.uploadContentImages(contentImages)
        communityService.createCommunity(userId, communityRequest, contentImagePairs)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
