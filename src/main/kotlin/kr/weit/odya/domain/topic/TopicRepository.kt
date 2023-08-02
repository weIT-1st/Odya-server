package kr.weit.odya.domain.topic

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun TopicRepository.getByTopicId(topicId: Long): Topic =
    findByIdOrNull(topicId) ?: throw NoSuchElementException("$topicId : 해당 토픽이 존재하지 않습니다.")

interface TopicRepository : JpaRepository<Topic, Long>
