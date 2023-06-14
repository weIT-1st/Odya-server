package kr.weit.odya.service

import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.security.FirebaseTokenParser
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.UserResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val firebaseTokenParser: FirebaseTokenParser
) {
    fun getInformation(userId: Long): UserResponse {
        val findUser = userRepository.getByUserId(userId)
        return UserResponse(findUser)
    }

    @Transactional
    fun updateEmail(userId: Long, idToken: String) {
        val email = firebaseTokenParser.getEmail(idToken)
        if (userRepository.existsByEmail(email)) {
            throw ExistResourceException("$email: 이미 존재하는 이메일입니다")
        }

        val findUser = userRepository.getByUserId(userId)
        findUser.changeEmail(email)
    }

    @Transactional
    fun updatePhoneNumber(userId: Long, idToken: String) {
        val phoneNumber = firebaseTokenParser.getPhoneNumber(idToken)
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw ExistResourceException("$phoneNumber: 이미 존재하는 전화번호입니다")
        }

        val findUser = userRepository.getByUserId(userId)
        findUser.changePhoneNumber(phoneNumber)
    }

    @Transactional
    fun updateInformation(userId: Long, informationRequest: InformationRequest) {
        validateInformationRequest(informationRequest)

        val findUser = userRepository.getByUserId(userId)
        findUser.changeInformation(informationRequest.nickname)
    }

    private fun validateInformationRequest(informationRequest: InformationRequest) {
        if (userRepository.existsByNickname(informationRequest.nickname)) {
            throw ExistResourceException("${informationRequest.nickname}: 이미 존재하는 닉네임입니다")
        }
    }
}
