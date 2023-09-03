package kr.weit.odya.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class FirebaseCloudMessageService(
    private val firebaseCloudMessage: FirebaseMessaging,
) {
    @Async
    fun sendPushNotification(request: PushNotificationRequest) {
        if (request.tokens.isEmpty()) {
            return
        }

        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(request.title)
                    .setBody(request.body)
                    .build(),
            )
            .apply {
                for (entry in request.data.entries) {
                    putData(entry.key, entry.value)
                }
            }
            .addAllTokens(request.tokens)
            .build()

        firebaseCloudMessage.sendMulticast(message)
    }
}

data class PushNotificationRequest(
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
