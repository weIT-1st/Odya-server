package kr.weit.odya.service

import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUsername
import kr.weit.odya.service.dto.UserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    fun getInformation(username: String): UserResponse {
        val findUser = userRepository.getByUsername(username)
        return UserResponse(findUser)
    }
}
