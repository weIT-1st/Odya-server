package kr.weit.odya.service

import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getTopicList
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.stereotype.Service

@Service
class TopicService(
    private val topicRepository: TopicRepository,
) {
    fun getTopicList(size: Int, lastId: Long?): SliceResponse<Topic> {
        return SliceResponse(size, topicRepository.getTopicList(size, lastId))
    }
}
