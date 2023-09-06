package kr.weit.odya.listener

import kr.weit.odya.client.push.FirebaseCloudMessageClient
import kr.weit.odya.client.push.PushNotificationEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ApplicationEventListener(
    private val firebaseCloudMessageClient: FirebaseCloudMessageClient,
) {
    @Async
    @EventListener
    fun listenPushNotificationEvent(event: PushNotificationEvent) {
        firebaseCloudMessageClient.sendPushNotification(event)
    }
}
