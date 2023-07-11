package kr.weit.odya.service

import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistory
import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistoryRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class PlaceSearchHistoryService(
    private val placeSearchHistoryRepository: PlaceSearchHistoryRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun saveSearchHistory(searchTerm: String, userId: Long) {
        val user = userRepository.getByUserId(userId)
        placeSearchHistoryRepository.save(PlaceSearchHistory(searchTerm, getAgeRange(user)))
    }

    fun getAgeRange(user: User): Int {
        val now = LocalDate.now()
        val birthYear = user.birthday
        var age = now.year - birthYear.year
        if (birthYear.plusYears(age.toLong()) < now) {
            age -= 1
        }
        return age / 10
    }

    fun getSearchHistory(): MutableIterable<PlaceSearchHistory> {
        return placeSearchHistoryRepository.findAll()
    }
}
