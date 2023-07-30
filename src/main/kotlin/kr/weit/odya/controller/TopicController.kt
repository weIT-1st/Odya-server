package kr.weit.odya.controller

import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.service.TopicService
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/topics")
class TopicController(private val topicService: TopicService) {
    @GetMapping("/list")
    fun getTopicList(
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "11", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
    ): ResponseEntity<SliceResponse<Topic>> {
        return ResponseEntity.ok(topicService.getTopicList(size, lastId))
    }
}
