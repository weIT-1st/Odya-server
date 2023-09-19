package kr.weit.odya.listener

import kr.weit.odya.domain.community.CommunityUpdateEvent
import kr.weit.odya.service.FileService
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.support.log.Logger
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class CommunityEventListener(
    private val fileService: FileService,
) {
    @Async
    @EventListener
    fun communityDeleteEventHandle(event: CommunityDeleteEvent) {
        try {
            event.deletedCommunityContentImageNames.forEach {
                fileService.deleteFile(it)
            }
        } catch (ex: ObjectStorageException) {
            Logger.error(ex) { "[communityDeleteEventHandle] ObjectStorageException: ${ex.message})]" }
        }
    }

    @Async
    @EventListener
    fun communityUpdateEventHandle(event: CommunityUpdateEvent) {
        try {
            event.deletedCommunityContentImageNames.forEach {
                fileService.deleteFile(it)
            }
        } catch (ex: ObjectStorageException) {
            Logger.error(ex) { "[communityUpdateEventHandle] ObjectStorageException: ${ex.message})]" }
        }
    }
}
