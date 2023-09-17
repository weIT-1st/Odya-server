package kr.weit.odya.listener

import kr.weit.odya.domain.traveljournal.TravelJournalContentUpdateEvent
import kr.weit.odya.domain.traveljournal.TravelJournalDeleteEvent
import kr.weit.odya.service.FileService
import kr.weit.odya.service.ObjectStorageException
import kr.weit.odya.support.log.Logger
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class TravelJournalEventListener(
    private val fileService: FileService,
) {
    @Async
    @EventListener
    fun travelJournalDeleteEventHandle(event: TravelJournalDeleteEvent) {
        try {
            event.deletedTravelJournalContentImageNames.forEach {
                fileService.deleteFile(it)
            }
        } catch (ex: ObjectStorageException) {
            Logger.error(ex) { "[travelJournalDeleteEventHandle] ObjectStorageException: ${ex.message})]" }
        }
    }

    @Async
    @EventListener
    fun travelJournalUpdateEventHandle(event: TravelJournalContentUpdateEvent) {
        try {
            event.deletedTravelJournalContentImageNames.forEach {
                fileService.deleteFile(it)
            }
        } catch (ex: ObjectStorageException) {
            Logger.error(ex) { "[travelJournalUpdateEventHandle] ObjectStorageException: ${ex.message})]" }
        }
    }
}
