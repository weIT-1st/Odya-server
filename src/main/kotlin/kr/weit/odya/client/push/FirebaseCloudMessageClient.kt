package kr.weit.odya.client.push

import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.ApsAlert
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
        // data만 보내면 iOS는 받지 못해서 iOS를 위한 설정
        val apnsConfig =
            ApnsConfig.builder().setAps(
                Aps.builder()
                    .setContentAvailable(true)
                    .setAlert(
                        ApsAlert
                            .builder()
                            .setTitle(event.title)
                            .setBody(event.body)
                            .build(),
                    ).build(),
            ).build()

        val message = MulticastMessage.builder()
            .apply {
                putAllData(event.data)
            }
            .setApnsConfig(apnsConfig)
            .addAllTokens(event.tokens)
            .build()

        firebaseCloudMessage.sendMulticast(message)
    }
}
