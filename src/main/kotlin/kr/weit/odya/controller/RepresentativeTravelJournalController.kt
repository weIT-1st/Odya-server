package kr.weit.odya.controller

import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalSortType
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.RepresentativeTravelJournalService
import kr.weit.odya.service.dto.RepTravelJournalSummaryResponse
import kr.weit.odya.service.dto.SliceResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/rep-travel-journals")
class RepresentativeTravelJournalController(
    private val repTravelJournalService: RepresentativeTravelJournalService,
) {
    @PostMapping("/{travelJournalId}")
    fun createRepTravelJournal(
        @LoginUserId userId: Long,
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
    ): ResponseEntity<Void> {
        repTravelJournalService.createRepTravelJournal(userId, travelJournalId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/me")
    fun getMyRepTravelJournals(
        @LoginUserId userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: RepresentativeTravelJournalSortType,
    ): ResponseEntity<SliceResponse<RepTravelJournalSummaryResponse>> {
        val response =
            repTravelJournalService.getMyRepTravelJournals(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{targetUserId}")
    fun getTargetRepTravelJournals(
        @LoginUserId
        loginUserId: Long,
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("targetUserId")
        targetUserId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: RepresentativeTravelJournalSortType,
    ): ResponseEntity<SliceResponse<RepTravelJournalSummaryResponse>> {
        val response =
            repTravelJournalService.getTargetRepTravelJournals(loginUserId, targetUserId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{repTravelJournalId}")
    fun deleteRepTravelJournal(
        @LoginUserId userId: Long,
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("repTravelJournalId")
        repTravelJournalId: Long,
    ): ResponseEntity<Void> {
        repTravelJournalService.deleteRepTravelJournal(userId, repTravelJournalId)
        return ResponseEntity.noContent().build()
    }
}
