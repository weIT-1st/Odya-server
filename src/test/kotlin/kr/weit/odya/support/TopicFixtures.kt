package kr.weit.odya.support

import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.AddFavoriteTopicRequest
import kr.weit.odya.service.dto.FavoriteTopicListResponse
import kr.weit.odya.service.dto.TopicResponse

const val TEST_TOPIC_ID = 1L
const val TEST_OTHER_TOPIC_ID = 2L
const val TEST_UPDATE_TOPIC_ID = 3L
const val TEST_NOT_EXIST_TOPIC_ID = 5L
const val TEST_INVALID_TOPIC_ID = -1L
const val TEST_TOPIC = "바다 여행"
const val TEST_OTHER_TOPIC = "추억 팔이"
const val TEST_FAVORITE_TOPIC_ID = 1L
const val TEST_INVALID_FAVORITE_TOPIC_ID = -1L
const val TEST_OTHER_FAVORITE_TOPIC_ID = 2L
const val TEST_NOT_EXIST_FAVORITE_TOPIC_ID = 9999L

fun createTopic(id: Long = TEST_TOPIC_ID) = Topic(id = id, word = TEST_TOPIC)
fun createOtherTopic() = Topic(id = TEST_OTHER_TOPIC_ID, word = TEST_OTHER_TOPIC)
fun createTopicList() = listOf(createTopic(), createOtherTopic())
fun createAddFavoriteTopicRequest() = AddFavoriteTopicRequest(listOf(TEST_TOPIC_ID, TEST_OTHER_TOPIC_ID))
fun createInvalidAddFavoriteTopicRequest() = AddFavoriteTopicRequest(listOf(TEST_INVALID_TOPIC_ID, TEST_TOPIC_ID))
fun createFavoriteTopic(user: User = createUser(), topic: Topic = createTopic()) =
    FavoriteTopic(TEST_FAVORITE_TOPIC_ID, user, topic)

fun createFavoriteTopicList() =
    listOf(createFavoriteTopic(), FavoriteTopic(TEST_OTHER_FAVORITE_TOPIC_ID, createUser(), createOtherTopic()))

fun createFavoriteTopicListResponse() = createFavoriteTopicList().map { FavoriteTopicListResponse(it) }

fun createTopicResponse() = TopicResponse(TEST_TOPIC_ID, TEST_TOPIC)

fun createTopicResponseList() = listOf(createTopicResponse(), TopicResponse(TEST_OTHER_TOPIC_ID, TEST_OTHER_TOPIC))
