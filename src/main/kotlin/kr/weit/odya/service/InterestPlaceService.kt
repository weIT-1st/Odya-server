package kr.weit.odya.service

import jakarta.transaction.Transactional
import kr.weit.odya.domain.interestPlace.InterestPlaceRepository
import kr.weit.odya.domain.interestPlace.getByInterestPlaceId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.InterestPlaceRequest
import org.springframework.stereotype.Service

@Service
class InterestPlaceService(private val interestPlaceRepository: InterestPlaceRepository, private val userRepository: UserRepository) {
    @Transactional
    fun createInterestPlace(userId: Long, request: InterestPlaceRequest) {
        if (interestPlaceRepository.existsByUserIdAndPlaceId(userId, request.placeId)) {
            throw ExistResourceException("${request.placeId}: 해당 장소는 이미 관심 장소입니다")
        }
        interestPlaceRepository.save(request.toEntity(userRepository.getByUserId(userId)))
    }

    @Transactional
    fun deleteInterestPlace(userId: Long, interestPlaceId: Long) {
        interestPlaceRepository.delete(interestPlaceRepository.getByInterestPlaceId(interestPlaceId))
    }

    fun getInterestPlace(userId: Long, placeId: String): Boolean {
        return interestPlaceRepository.existsByUserIdAndPlaceId(userId, placeId)
    }
}
