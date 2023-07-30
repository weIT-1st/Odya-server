package kr.weit.odya.support

import kr.weit.odya.domain.topic.Topic

const val TEST_TOPIC_ID = 1L
const val TEST_OTHER_TOPIC_ID = 2L
const val TEST_TOPIC = "바다 여행"
const val TEST_OTHER_TOPIC = "추억 팔이"

fun createTopic() = Topic(id = TEST_TOPIC_ID, word = TEST_TOPIC)
fun createOtherTopic() = Topic(id = TEST_OTHER_TOPIC_ID, word = TEST_OTHER_TOPIC)
