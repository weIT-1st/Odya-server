package kr.weit.odya.domain.topic

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createOtherTopic
import kr.weit.odya.support.createTopic
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TopicRepositoryTest(
    private val topicRepository: TopicRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        lateinit var topic: Topic
        lateinit var otherTopic: Topic
        beforeEach {
            topic = topicRepository.save(createTopic())
            otherTopic = topicRepository.save(createOtherTopic())
        }

        context("토픽 조회") {
            expect("토픽 전체를 조회한다.") {
                val result = topicRepository.findAll()
                result.size shouldBe 2
            }
            expect("토픽 ID와 일치하는 토픽을 조회한다.") {
                val result = topicRepository.getByTopicId(topic.id)
                result.id shouldBe topic.id
            }
        }
    },
)
