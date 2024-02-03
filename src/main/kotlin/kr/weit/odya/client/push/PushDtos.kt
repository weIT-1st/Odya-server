package kr.weit.odya.client.push

import kr.weit.odya.service.dto.UserProfileResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class PushNotificationEvent(
    val tokens: List<String>,
    val data: Map<String, String>,
) {
    constructor(
        title: String,
        body: String,
        tokens: List<String>,
        userName: String,
        userProfileUrl: String,
        userProfileColor: UserProfileResponse.ProfileColorResponse?,
        eventType: NotificationEventType,
        contentImage: String? = null,
        communityId: Long? = null,
        travelJournalId: Long? = null,
        content: String? = null,
        followerId: Long? = null,
    ) : this(
        tokens = tokens,
        data = mutableMapOf(
            "title" to title,
            "body" to body,
            "userName" to userName,
            "eventType" to eventType.name,
            "userProfileUrl" to userProfileUrl,
            "notifiedAt" to LocalDateTime.now().format(dateTimeFormatter),
        ).apply {
            userProfileColor?.let { profileColor ->
                put("profileColorHex", profileColor.colorHex)
                put("profileColorRed", profileColor.red.toString())
                put("profileColorGreen", profileColor.green.toString())
                put("profileColorBlue", profileColor.blue.toString())
            }
            communityId?.toString()?.let { put("communityId", it) }
            travelJournalId?.toString()?.let { put("travelJournalId", it) }
            content?.let { put("content", it) }
            followerId?.toString()?.let { put("followerId", it) }
            contentImage?.let { put("contentImage", it) }
        },
    )

    companion object {
        // 멀티쓰레드 환경에서 안전하지 않은 SimpleDateFormat 대신 사용
        private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}

enum class NotificationEventType {
    COMMUNITY_COMMENT,
    COMMUNITY_LIKE,
    FOLLOWER_ADD,
    FOLLOWING_COMMUNITY,
    FOLLOWING_TRAVEL_JOURNAL,
    TRAVEL_JOURNAL_TAG,
}
