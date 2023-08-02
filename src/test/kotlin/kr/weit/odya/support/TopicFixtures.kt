package kr.weit.odya.support

import kr.weit.odya.domain.favoriteTopic.FavoriteTopic
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.user.User

const val TEST_TOPIC_ID = 1L
const val TEST_OTHER_TOPIC_ID = 2L
const val TEST_TOPIC = "바다 여행"
const val TEST_OTHER_TOPIC = "추억 팔이"
const val TEST_FAVORITE_TOPIC_ID = 1L

fun createTopic() = Topic(id = TEST_TOPIC_ID, word = TEST_TOPIC)
fun createOtherTopic() = Topic(id = TEST_OTHER_TOPIC_ID, word = TEST_OTHER_TOPIC)
fun createTopicList() = listOf(createTopic())
fun createFavoriteTopic(user: User = createUser(), topic: Topic = createTopic()) = FavoriteTopic(TEST_FAVORITE_TOPIC_ID, user, topic)
