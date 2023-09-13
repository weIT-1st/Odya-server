package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.ImageService
import kr.weit.odya.service.dto.ImageResponse
import kr.weit.odya.service.dto.LifeShotRequest
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/images")
class ImageController(
    private val imageService: ImageService,
) {

    @GetMapping
    fun getImages(
        @LoginUserId
        userId: Long,
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<ImageResponse>> {
        return ResponseEntity.ok(imageService.getImages(userId, size, lastId))
    }

    @PostMapping("/{imageId}/life-shot")
    fun setLifeShot( // 인생샷 설정 뿐 아니라 장소 업데이트도 이 API로 처리한다
        @LoginUserId
        userId: Long,
        @Positive(message = "이미지 ID는 양수여야 합니다.")
        @PathVariable(name = "imageId")
        imageId: Long,
        @Valid
        @RequestBody
        request: LifeShotRequest,
    ): ResponseEntity<Void> {
        imageService.setLifeShot(userId, imageId, request)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{imageId}/life-shot")
    fun cancelLifeShot(
        @LoginUserId
        userId: Long,
        @Positive(message = "이미지 ID는 양수여야 합니다.")
        @PathVariable(name = "imageId")
        imageId: Long,
    ): ResponseEntity<Void> {
        imageService.cancelLifeShot(userId, imageId)
        return ResponseEntity.noContent().build()
    }
}
