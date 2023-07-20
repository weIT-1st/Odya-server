package kr.weit.odya.support

import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistory
import kr.weit.odya.service.dto.PlaceSearchHistoryRequest

const val TEST_PLACE_SEARCH_TERM = "서울"
const val TEST_AGE_RANGE = 2
const val TEST_INVALID_AGE_RANGE = -2

fun createPlaceSearchHistory(): PlaceSearchHistory {
    return PlaceSearchHistory(TEST_PLACE_SEARCH_TERM, TEST_AGE_RANGE)
}

fun createPlaceSearchRequest(): PlaceSearchHistoryRequest {
    return PlaceSearchHistoryRequest(TEST_PLACE_SEARCH_TERM)
}

fun createListSearchTerm(): List<String> {
    return listOf(TEST_PLACE_SEARCH_TERM)
}
