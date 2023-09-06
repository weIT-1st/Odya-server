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
        token: String,
        data: Map<String, String>,
    ) : this(title, body, listOf(token), data)
}
