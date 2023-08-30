package kr.weit.odya.controller

import jakarta.validation.Valid
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.ReportService
import kr.weit.odya.service.dto.ReportPlaceReviewRequest
import kr.weit.odya.service.dto.ReportReasonsResponse
import kr.weit.odya.service.dto.ReportTravelJournalRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/reports")
class ReportController(private val reportService: ReportService) {
    @GetMapping
    fun getReportReasons(): ResponseEntity<List<ReportReasonsResponse>> {
        return ResponseEntity.ok(reportService.getReportReasons())
    }

    @PostMapping("/place-review")
    fun reportPlaceReview(
        @LoginUserId
        userId: Long,
        @Valid
        @RequestBody
        request: ReportPlaceReviewRequest,
    ): ResponseEntity<Void> {
        reportService.reportPlaceReview(userId, request)
        reportService.checkReportPlaceReviewCount(request.placeReviewId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    @PostMapping("/travel-Journal")
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
}
