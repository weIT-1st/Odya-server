package kr.weit.odya.domain.topic

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.createOtherTopic
import kr.weit.odya.support.createTopic
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TopicRepositoryTest(
    private val topicRepository: TopicRepository,
) : ExpectSpec(
    {
        beforeEach {
            topicRepository.save(createTopic())
            topicRepository.save(createOtherTopic())
        }

        context("lastId 이후의 토픽 조회") {
            expect("토픽을 조회한다.") {
                val result = topicRepository.getTopic(TEST_DEFAULT_SIZE, null)
                result.size shouldBe 2
            }

            expect("토픽을 조회한다.") {
                val result = topicRepository.getTopic(TEST_DEFAULT_SIZE, 1L)
                result.size shouldBe 1
            }
        }
    },
)
