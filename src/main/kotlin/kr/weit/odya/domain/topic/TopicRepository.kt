package kr.weit.odya.domain.topic

import org.springframework.data.jpa.repository.JpaRepository

interface TopicRepository : JpaRepository<Topic, Long>
