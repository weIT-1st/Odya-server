package kr.weit.odya.client.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
import com.google.firebase.messaging.Notification
import org.springframework.stereotype.Component

@Component
class FirebaseCloudMessageClient(
    private val firebaseCloudMessage: FirebaseMessaging,
) {
    fun sendPushNotification(event: PushNotificationEvent) {
        if (event.tokens.isEmpty()) {
            return
        }

        val message = MulticastMessage.builder()
            .setNotification(
                Notification.builder()
                    .setTitle(event.title)
                    .setBody(event.body)
                    .build(),
            )
            .apply {
                    putAllData(event.data)
            }
            .addAllTokens(event.tokens)
            .build()

        firebaseCloudMessage.sendMulticast(message)
    }
}
