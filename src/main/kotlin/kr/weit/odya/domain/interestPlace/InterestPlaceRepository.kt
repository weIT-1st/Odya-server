package kr.weit.odya.domain.interestPlace

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun InterestPlaceRepository.getByInterestPlaceId(interestPlaceId: Long): InterestPlace =
    findByIdOrNull(interestPlaceId) ?: throw NoSuchElementException("해당 장소는 관심 장소가 아닙니다")

@Repository
interface InterestPlaceRepository : JpaRepository<InterestPlace, Long> {
    fun existsByUserIdAndPlaceId(userId: Long, placeId: String): Boolean
}
