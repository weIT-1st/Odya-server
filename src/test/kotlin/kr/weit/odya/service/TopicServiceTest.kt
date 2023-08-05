package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.favoriteTopic.FavoriteTopicRepository
import kr.weit.odya.domain.favoriteTopic.getByFavoriteTopicId
import kr.weit.odya.domain.favoriteTopic.getByUserId
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.AddFavoriteTopicRequest
import kr.weit.odya.support.TEST_FAVORITE_TOPIC_ID
import kr.weit.odya.support.TEST_INVALID_TOPIC_ID
import kr.weit.odya.support.TEST_NOT_EXIST_FAVORITE_TOPIC_ID
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_TOPIC_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createAddFavoriteTopicRequest
import kr.weit.odya.support.createFavoriteTopic
import kr.weit.odya.support.createFavoriteTopicList
import kr.weit.odya.support.createInvalidAddFavoriteTopicRequest
import kr.weit.odya.support.createTopic
import kr.weit.odya.support.createTopicList
import kr.weit.odya.support.createUser

class TopicServiceTest : DescribeSpec(
    {
        val topicRepository = mockk<TopicRepository>()
        val favoriteTopicRepository = mockk<FavoriteTopicRepository>()
        val userRepository = mockk<UserRepository>()
        val topicService = TopicService(topicRepository, favoriteTopicRepository, userRepository)
        val user = createUser()

        describe("getTopicList 메소드") {
            context("토픽이 정상적으로 있을 경우") {
                every { topicRepository.findAll() } returns createTopicList()
                it("등록된 토픽이 반환된다") {
                    shouldNotThrowAny { topicService.getTopicList() }
                }
            }
        }

        describe("addFavoriteTopic 메소드") {
            context("유효한 userId와 topicId 리스트가 주어졌을 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { favoriteTopicRepository.existsByUserAndTopicId(user, TEST_TOPIC_ID) } returns false
                every { topicRepository.getByTopicId(TEST_TOPIC_ID) } returns createTopic()
                every { favoriteTopicRepository.saveAll(listOf(FavoriteTopic(any(), any(), any()))) } returns createFavoriteTopicList()
                it("관심 토픽으로 등록한다") {
                    shouldNotThrowAny { topicService.addFavoriteTopic(TEST_USER_ID, AddFavoriteTopicRequest(listOf(TEST_TOPIC_ID))) }
                }
            }

            context("존재하지않는 userId가 주어졌을 경우") {
                every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                it("NoSuchElementException 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { topicService.addFavoriteTopic(TEST_NOT_EXIST_USER_ID, createAddFavoriteTopicRequest()) }
                }
            }

            context("유효한 userId와 존재하지 않는 topicId가 주어졌을 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { favoriteTopicRepository.existsByUserAndTopicId(user, TEST_INVALID_TOPIC_ID) } returns false
                every { topicRepository.getByTopicId(TEST_INVALID_TOPIC_ID) } throws NoSuchElementException()
                it("NoSuchElementException 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { topicService.addFavoriteTopic(TEST_USER_ID, createInvalidAddFavoriteTopicRequest()) }
                }
            }
        }

        describe("deleteFavoriteTopic 메소드") {
            context("유효한 userId와 favoriteTopicId가 주어졌을 경우") {
                every { favoriteTopicRepository.getByFavoriteTopicId(TEST_FAVORITE_TOPIC_ID) } returns createFavoriteTopic(user)
                every { favoriteTopicRepository.delete(any()) } returns Unit
                it("관심 토픽을 삭제한다") {
                    shouldNotThrowAny { topicService.deleteFavoriteTopic(TEST_USER_ID, TEST_FAVORITE_TOPIC_ID) }
                }
            }

            context("존재하지 않는 favoriteTopicId가 주어졌을 경우") {
                every { favoriteTopicRepository.getByFavoriteTopicId(TEST_NOT_EXIST_FAVORITE_TOPIC_ID) } throws NoSuchElementException()
                it("관심 토픽을 삭제한다") {
                    shouldThrow<NoSuchElementException> { topicService.deleteFavoriteTopic(TEST_USER_ID, TEST_NOT_EXIST_FAVORITE_TOPIC_ID) }
                }
            }

            context("삭제할 권한이 없는 userId가 주어졌을 경우") {
                every { favoriteTopicRepository.getByFavoriteTopicId(TEST_FAVORITE_TOPIC_ID) } returns createFavoriteTopic(user)
                it("관심 토픽을 삭제한다") {
                    shouldThrow<ForbiddenException> { topicService.deleteFavoriteTopic(TEST_NOT_EXIST_USER_ID, TEST_FAVORITE_TOPIC_ID) }
                }
            }
        }

        describe("getFavoriteTopicList 메소드") {
            context("유효한 userId가 주어졌을 경우") {
                every { favoriteTopicRepository.getByUserId(TEST_USER_ID) } returns createFavoriteTopicList()
                it("해당 유저의 관심 토픽 리스트를 반환한다") {
                    shouldNotThrowAny { topicService.getFavoriteTopicList(TEST_USER_ID) }
                }
            }
        }
    },
)
