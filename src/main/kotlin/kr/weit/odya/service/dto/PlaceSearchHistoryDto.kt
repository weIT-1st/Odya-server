package kr.weit.odya.service.dto

data class PlaceSearchHistoryRequest(
    val searchTerm: String,
)

data class OverallRankingResponse(
    val searchTerm: String,
)
