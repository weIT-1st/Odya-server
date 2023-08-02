package kr.weit.odya.controller

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TopicService
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
        @NotBlank(message = "토픽 ID는 필수 입력값입니다.")
        @RequestBody
        topicIdList: List<Long>,
    ): ResponseEntity<Void> {
        topicService.addFavoriteTopic(userId, topicIdList)
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
    ): ResponseEntity<List<Topic>> {
        return ResponseEntity.ok(topicService.getFavoriteTopicList(userId))
    }
}
