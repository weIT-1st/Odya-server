package kr.weit.odya.service

import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.UserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    fun getInformation(userId: Long): UserResponse {
        val findUser = userRepository.getByUserId(userId)
        return UserResponse(findUser)
    }
}
