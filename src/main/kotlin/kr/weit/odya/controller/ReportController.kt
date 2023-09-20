package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.ReportService
import kr.weit.odya.service.dto.ReportCommunityRequest
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportTravelJournalRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/reports")
class ReportController(private val reportService: ReportService) {

    @PostMapping("/place-review")
    fun reportPlaceReview(
        @LoginUserId
        userId: Long,
        @Valid
        @RequestBody
        request: ReportPlaceReviewRequest,
    ): ResponseEntity<Void> {
        reportService.reportPlaceReview(userId, request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("/travel-journal")
    fun reportTravelJournal(
        @LoginUserId
        userId: Long,
        @Valid
        @RequestBody
        request: ReportTravelJournalRequest,
    ): ResponseEntity<Void> {
        reportService.reportTravelJournal(userId, request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("/community")
    fun reportCommunity(
        @LoginUserId
        userId: Long,
        @Valid
        @RequestBody
        request: ReportCommunityRequest,
    ): ResponseEntity<Void> {
        reportService.reportCommunity(userId, request)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
