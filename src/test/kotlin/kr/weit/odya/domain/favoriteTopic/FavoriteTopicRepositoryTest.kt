package kr.weit.odya.domain.favoriteTopic

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_NOT_EXIST_FAVORITE_TOPIC_ID
import kr.weit.odya.support.createFavoriteTopic
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class FavoriteTopicRepositoryTest(
    private val favoriteTopicRepository: FavoriteTopicRepository,
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var favoriteTopic: FavoriteTopic
        beforeEach {
            user = userRepository.save(createUser())
            favoriteTopic = favoriteTopicRepository.save(createFavoriteTopic(user))
        }

        context("관심 토픽 조회") {
            expect("favoriteTopicId와 일치하는 관심 장소를 조회한다") {
                val result = favoriteTopicRepository.getByFavoriteTopicId(favoriteTopic.id)
                result.id shouldBe favoriteTopic.id
            }

            expect("userId와 일치하는 관심 장소를 조회한다") {
                val result = favoriteTopicRepository.getByUserId(user.id)
                result.first().registrantsId shouldBe user.id
            }
        }

        context("관심 토픽 존재 여부") {
            expect("user와 topicId가 일치하는 관심 토픽 존재 여부를 확인한다(등록되어있음)") {
                val result = favoriteTopicRepository.existsByUserAndTopicId(user, favoriteTopic.topic.id)
                result shouldBe true
            }

            expect("user와 topicId가 일치하는 관심 토픽 존재 여부를 확인한다(등록되어있지 않음)") {
                val result = favoriteTopicRepository.existsByUserAndTopicId(user, TEST_NOT_EXIST_FAVORITE_TOPIC_ID)
                result shouldBe false
            }
        }

        context("유저의 관심 토픽 전체 삭제") {
            expect("userId와 일치하는 관심 토픽 전체를 삭제한다") {
                favoriteTopicRepository.deleteByUserId(user.id)
                val result = favoriteTopicRepository.getByUserId(user.id)
                result.size shouldBe 0
            }
        }
    },
)
