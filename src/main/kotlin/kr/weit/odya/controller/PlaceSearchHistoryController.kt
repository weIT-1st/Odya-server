package kr.weit.odya.controller

import jakarta.validation.constraints.Positive
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.PlaceSearchHistoryService
import kr.weit.odya.service.dto.PlaceSearchHistoryRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/api/v1/place-search-histories")
class PlaceSearchHistoryController(private val placeSearchHistoryService: PlaceSearchHistoryService) {
    @PostMapping()
    fun saveSearchHistory(
        @RequestBody
        placeSearchHistoryRequest: PlaceSearchHistoryRequest,
        @LoginUserId userId: Long,
    ): ResponseEntity<Void> {
        placeSearchHistoryService.saveSearchHistory(placeSearchHistoryRequest.searchTerm, userId)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping("/ranking")
    fun getOverallRank(): List<String> {
        return placeSearchHistoryService.getOverallRanking()
    }

    @GetMapping("/ranking/ageRange/{ageRange}")
    fun getRankByAgeRange(
        @LoginUserId
        userId: Long,
        @Positive(message = "연령대는 양수여야 합니다.")
        @PathVariable
        ageRange: Int?,
    ): List<String> {
        return placeSearchHistoryService.getAgeRangeRanking(userId, ageRange)
    }
}
