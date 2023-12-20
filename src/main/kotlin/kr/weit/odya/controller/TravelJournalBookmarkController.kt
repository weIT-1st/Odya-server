package kr.weit.odya.controller

import jakarta.validation.constraints.Positive
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkSortType
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.TravelJournalBookmarkService
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TravelJournalBookmarkSummaryResponse
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
@RequestMapping("/api/v1/travel-journal-bookmarks")
class TravelJournalBookmarkController(
    private val travelJournalBookmarkService: TravelJournalBookmarkService,
) {
    @PostMapping("/{travelJournalId}")
    fun createTravelJournalBookmark(
        @LoginUserId userId: Long,
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
    ): ResponseEntity<Void> {
        travelJournalBookmarkService.createTravelJournalBookmark(userId, travelJournalId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/me")
    fun getMyTravelJournalBookmarks(
        @LoginUserId userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: TravelJournalBookmarkSortType,
    ): ResponseEntity<SliceResponse<TravelJournalBookmarkSummaryResponse>> {
        val response =
            travelJournalBookmarkService.getMyTravelJournalBookmarks(userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{userId}")
    fun getOtherTravelJournalBookmarks(
        @LoginUserId
        loginUserId: Long,
        @Positive(message = "USER ID는 양수여야 합니다.")
        @PathVariable("userId")
        userId: Long,
        @Positive(message = "조회할 개수는 양수여야 합니다.")
        @RequestParam("size", defaultValue = "10", required = false)
        size: Int,
        @Positive(message = "마지막 ID는 양수여야 합니다.")
        @RequestParam("lastId", required = false)
        lastId: Long?,
        @RequestParam("sortType", defaultValue = "LATEST", required = false)
        sortType: TravelJournalBookmarkSortType,
    ): ResponseEntity<SliceResponse<TravelJournalBookmarkSummaryResponse>> {
        val response =
            travelJournalBookmarkService.getOtherTravelJournalBookmarks(loginUserId, userId, size, lastId, sortType)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{travelJournalId}")
    fun deleteTravelJournalBookmark(
        @LoginUserId userId: Long,
        @Positive(message = "travelJournalId는 0보다 커야합니다.")
        @PathVariable("travelJournalId")
        travelJournalId: Long,
    ): ResponseEntity<Void> {
        travelJournalBookmarkService.deleteTravelJournalBookmark(userId, travelJournalId)
        return ResponseEntity.noContent().build()
    }
}
