package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TopicService
import kr.weit.odya.service.dto.AddFavoriteTopicRequest
import kr.weit.odya.service.dto.FavoriteTopicListResponse
import kr.weit.odya.service.dto.TopicResponse
import org.springframework.http.HttpStatus
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
@RequestMapping("/api/v1/topics")
class TopicController(private val topicService: TopicService) {
    @GetMapping
    fun getTopicList(): ResponseEntity<List<Topic>> {
        return ResponseEntity.ok(topicService.getTopicList())
    }

    @PostMapping
    fun createFavoritePlace(
        @LoginUserId
        userId: Long,
        @Valid
        @RequestBody
        request: AddFavoriteTopicRequest,
    ): ResponseEntity<Void> {
        topicService.addFavoriteTopic(userId, request)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @DeleteMapping("/{id}")
    fun deleteFavoritePlace(
        @LoginUserId
        userId: Long,
        @NotNull(message = "관심 토픽 ID는 필수 입력값입니다.")
        @Positive(message = "관심 토픽 ID는 양수여야 합니다.")
        @PathVariable("id")
        favoriteTopicId: Long,
    ): ResponseEntity<Void> {
        topicService.deleteFavoriteTopic(userId, favoriteTopicId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @GetMapping("/favorite")
    fun getFavoriteTopicList(
        @LoginUserId
        userId: Long,
    ): ResponseEntity<List<FavoriteTopicListResponse>> {
        return ResponseEntity.ok(topicService.getFavoriteTopicList(userId))
    }

    @GetMapping("/{placeId}")
    fun getPopularTopicsAtPlace(
        @NotBlank(message = "장소 ID는 필수입니다.")
        @PathVariable
        placeId: String,
        @Positive(message = "조회 개수는 1 이상이어야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "5")
        size: Int,
    ): ResponseEntity<List<TopicResponse>> {
        return ResponseEntity.ok(topicService.getPopularTopicsAtPlace(placeId, size))
    }
}
