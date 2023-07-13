package kr.weit.odya.service

import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistory
import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistoryRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import org.springframework.stereotype.Service

@Service
class PlaceSearchHistoryService(
    private val placeSearchHistoryRepository: PlaceSearchHistoryRepository,
    private val userRepository: UserRepository,
) {
    fun saveSearchHistory(searchTerm: String, userId: Long) {
        val ageRange = userRepository.getByUserId(userId).getAgeRange()
        placeSearchHistoryRepository.save(PlaceSearchHistory(searchTerm, ageRange))
    }

    fun getOverallRanking(): List<String> {
        return placeSearchHistoryRepository.getRecentTop10Keywords(null)
    }

    fun getAgeRangeRanking(userId: Long, ageRange: Int?): List<String> {
        val ageRange = ageRange ?: userRepository.getByUserId(userId).getAgeRange()
        return placeSearchHistoryRepository.getRecentTop10Keywords(ageRange)
    }
}
