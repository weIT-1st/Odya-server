package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Size
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TravelJournalService
import kr.weit.odya.service.dto.TravelJournalRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("api/v1/travel-journals")
class TravelJournalController(private val travelJournalService: TravelJournalService) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createTravelJournal(
        @LoginUserId userId: Long,
        @Valid
        @RequestPart("travel-journal")
        travelJournalRequest: TravelJournalRequest,
        @Size(min = 1, max = 225, message = "이미지는 최소 1개, 최대 225개까지 업로드할 수 있습니다.")
        @RequestPart("travel-journal-content-image")
        images: List<MultipartFile>,
    ): ResponseEntity<Void> {
        val imageMap = travelJournalService.getImageMap(images)
        travelJournalService.validateTravelJournalRequest(travelJournalRequest, imageMap)
        val imageNamePairs = travelJournalService.uploadTravelContentImages(travelJournalRequest, imageMap)
        travelJournalService.createTravelJournal(userId, travelJournalRequest, imageNamePairs)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}
