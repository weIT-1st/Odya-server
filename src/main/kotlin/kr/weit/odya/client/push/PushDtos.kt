package kr.weit.odya.client.push

data class PushNotificationEvent(
    val title: String,
    val body: String,
    val tokens: List<String>,
    val data: Map<String, String>,
) {
    constructor(
        title: String,
        body: String,
        tokens: List<String>,
        userName: String,
        eventType: NotificationEventType,
        communityId: Long? = null,
        travelJournalId: Long? = null,
        content: String? = null,
        followerId: Long? = null,
    ) : this(
        title = title,
        body = body,
        tokens = tokens,
        data = mutableMapOf(
            "userName" to userName,
            "eventType" to eventType.name,
        ).apply {
            communityId?.toString()?.let { put("communityId", it) }
            travelJournalId?.toString()?.let { put("travelJournalId", it) }
            content?.let { put("content", it) }
            followerId?.toString()?.let { put("followerId", it) }
        },
    )
}

enum class NotificationEventType {
    COMMUNITY_COMMENT,
    COMMUNITY_LIKE,
    FOLLOWER_ADD,
    FOLLOWING_COMMUNITY,
    FOLLOWING_TRAVEL_JOURNAL,
    TRAVEL_JOURNAL_TAG,
}
