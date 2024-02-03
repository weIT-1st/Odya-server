package kr.weit.odya.client.push

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.MulticastMessage
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
            .apply {
                putAllData(event.data)
            }
            .addAllTokens(event.tokens)
            .build()

        firebaseCloudMessage.sendMulticast(message)
    }
}
