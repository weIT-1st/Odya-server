package kr.weit.odya.controller

import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistory
import kr.weit.odya.security.LoginUserId
import kr.weit.odya.service.PlaceSearchHistoryService
import kr.weit.odya.service.dto.PlaceSearchHistoryDto.PlaceSearchHistoryRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
        return ResponseEntity.ok().build()
    }

    @GetMapping()
    fun getSearchHistory(
        @LoginUserId userId: Long,
    ): MutableIterable<PlaceSearchHistory> {
        return placeSearchHistoryService.getSearchHistory()
    }
}
