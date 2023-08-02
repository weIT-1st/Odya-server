/*
package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getTopicList
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.createTopicList

class TopicServiceTest : DescribeSpec(
    {
        val topicRepository = mockk<TopicRepository>()
        val topicService = TopicService(topicRepository)

        describe("getTopicList 메소드") {
            context("토픽이 정상적으로 있을 경우") {
                every { topicRepository.getTopicList(TEST_DEFAULT_SIZE, null) } returns createTopicList()
                it("등록된 토픽이 반환된다") {
                    shouldNotThrowAny { topicService.getTopicList(TEST_DEFAULT_SIZE, null) }
                }
            }
        }
    },
)
*/
