package kr.weit.odya.controller

import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import kr.weit.odya.domain.traveljournal.TravelJournalSortType
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TravelJournalService
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TaggedTravelJournalResponse
import kr.weit.odya.service.dto.TravelJournalContentUpdateRequest
import kr.weit.odya.service.dto.TravelJournalRequest
import kr.weit.odya.service.dto.TravelJournalResponse
import kr.weit.odya.service.dto.TravelJournalSummaryResponse
import kr.weit.odya.service.dto.TravelJournalUpdateRequest
import kr.weit.odya.support.validator.NullOrNotBlank
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.net.URI

@Validated
@RestController
@RequestMapping("/api/v1/travel-journals")
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
        val placeDetailsMap =
            travelJournalService.getPlaceDetailsMap(
                travelJournalRequest.travelJournalContentRequests.mapNotNull { it.placeId }
                    .toSet(),
            )
        val imageMap = travelJournalService.getImageMap(images)
        travelJournalService.validateTravelJournalRequest(travelJournalRequest, imageMap)
        val imageNamePairs = travelJournalRequest.travelJournalContentRequests.flatMap { travelJournalContentRequest ->
            travelJournalService.uploadTravelContentImages(travelJournalContentRequest.contentImageNames, imageMap)
        }
        val createdTravelJournalId =
            travelJournalService.createTravelJournal(userId, travelJournalRequest, imageNamePairs, placeDetailsMap)
        return ResponseEntity.created(URI.create("/api/v1/travel-journals/$createdTravelJournalId")).build()
    }

    @GetMapping("/{travelJournalId}")
    fun getTravelJournal(
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<TravelJournalResponse> {
        val response = travelJournalService.getTravelJournal(travelJournalId, userId)
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getTravelJournals(
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST")
        sortType: TravelJournalSortType,
        @LoginUserId userId: Long,
    ): ResponseEntity<SliceResponse<TravelJournalSummaryResponse>> {
        val response = travelJournalService.getTravelJournals(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/me")
    fun getMyTravelJournals(
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
        @NullOrNotBlank(message = "placeId는 공백이면 안됩니다.")
        @RequestParam(name = "placeId", required = false)
        placeId: String?,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST")
        sortType: TravelJournalSortType,
        @LoginUserId userId: Long,
    ): ResponseEntity<SliceResponse<TravelJournalSummaryResponse>> {
        val response = travelJournalService.getMyTravelJournals(userId, size, lastId, placeId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/friends")
    fun getFriendTravelJournals(
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
        @NullOrNotBlank(message = "placeId는 공백이면 안됩니다.")
        @RequestParam(name = "placeId", required = false)
        placeId: String?,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST")
        sortType: TravelJournalSortType,
        @LoginUserId userId: Long,
    ): ResponseEntity<SliceResponse<TravelJournalSummaryResponse>> {
        val response = travelJournalService.getFriendTravelJournals(userId, size, lastId, placeId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/recommends")
    fun getRecommendTravelJournals(
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
        @NullOrNotBlank(message = "placeId는 공백이면 안됩니다.")
        @RequestParam(name = "placeId", required = false)
        placeId: String?,
        @RequestParam(name = "sortType", required = false, defaultValue = "LATEST")
        sortType: TravelJournalSortType,
        @LoginUserId userId: Long,
    ): ResponseEntity<SliceResponse<TravelJournalSummaryResponse>> {
        val response = travelJournalService.getRecommendTravelJournals(userId, size, lastId, placeId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/tagged")
    fun getTaggedTravelJournals(
        @Positive(message = "사이즈는 양수여야 합니다.")
        @RequestParam(name = "size", required = false, defaultValue = "10")
        size: Int,
        @Positive(message = "마지막 Id는 양수여야 합니다.")
        @RequestParam(name = "lastId", required = false)
        lastId: Long?,
        @LoginUserId userId: Long,
    ): ResponseEntity<SliceResponse<TaggedTravelJournalResponse>> {
        return ResponseEntity.ok(travelJournalService.getTaggedTravelJournals(userId, size, lastId))
    }

    @PutMapping(path = ["/{travelJournalId}"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateTravelJournal(
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
        @LoginUserId userId: Long,
        @Valid
        @RequestPart("travel-journal-update")
        travelJournalUpdateRequest: TravelJournalUpdateRequest,
    ): ResponseEntity<Void> {
        travelJournalService.updateTravelJournal(travelJournalId, userId, travelJournalUpdateRequest)
        return ResponseEntity.noContent().build()
    }

    @PutMapping(
        path = ["/{travelJournalId}/{travelJournalContentId}"],
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun updateTravelJournalContent(
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
        @Positive(message = "travelJournalContentId는 0보다 커야합니다.")
        @PathVariable("travelJournalContentId")
        travelJournalContentId: Long,
        @LoginUserId userId: Long,
        @Valid
        @RequestPart("travel-journal-content-update")
        travelJournalContentUpdateRequest: TravelJournalContentUpdateRequest,
        @RequestPart("travel-journal-content-image-update", required = false)
        images: List<MultipartFile>?,
    ): ResponseEntity<Void> {
        val placeDetailsMap =
            travelJournalService.getPlaceDetailsMap(
                travelJournalContentUpdateRequest.placeId?.let { setOf(it) }
                    ?: emptySet(),
            )
        val imageMap = travelJournalService.getImageMap(images)
        travelJournalService.validateTravelJournalContentUpdateRequest(
            travelJournalId,
            travelJournalContentId,
            userId,
            imageMap,
            travelJournalContentUpdateRequest,
        )
        val imageNamePairs = travelJournalService.uploadTravelContentImages(
            travelJournalContentUpdateRequest.updateContentImageNames ?: emptyList(),
            imageMap,
        )

        travelJournalService.updateTravelJournalContent(
            travelJournalId,
            travelJournalContentId,
            userId,
            travelJournalContentUpdateRequest,
            imageNamePairs,
            placeDetailsMap,
        )
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{travelJournalId}")
    fun deleteTravelJournal(
        @Positive(message = "travelJournalId는 1보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        travelJournalService.deleteTravelJournal(travelJournalId, userId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{travelJournalId}/{travelJournalContentId}")
    fun deleteTravelJournalContent(
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
        @Positive(message = "travelJournalContentId는 0보다 커야합니다.")
        @PathVariable("travelJournalContentId")
        travelJournalContentId: Long,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        travelJournalService.deleteTravelJournalContent(travelJournalId, travelJournalContentId, userId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{travelJournalId}/travelCompanion")
    fun removeTravelCompanion(
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
        @LoginUserId
        userId: Long,
    ): ResponseEntity<Void> {
        travelJournalService.removeTravelCompanion(userId, travelJournalId)
        return ResponseEntity.noContent().build()
    }
}
