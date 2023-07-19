package kr.weit.odya.domain.interestPlace

import kr.weit.odya.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface InterestPlaceRepository : JpaRepository<InterestPlace, Long> {
    fun existsByUserAndPlaceId(user: User, placeId: String): Boolean

    fun deleteByUserAndPlaceId(user: User, placeId: String)
}
