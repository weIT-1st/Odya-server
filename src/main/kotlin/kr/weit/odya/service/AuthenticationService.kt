package kr.weit.odya.service

import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.security.FirebaseTokenParser
import kr.weit.odya.service.dto.LoginRequest
import kr.weit.odya.service.dto.RegisterRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AuthenticationService(
    private val userRepository: UserRepository,
    private val firebaseTokenParser: FirebaseTokenParser
) {
    fun loginProcess(loginRequest: LoginRequest) {
        val username = firebaseTokenParser.getUsername(loginRequest.idToken)
        if (!userRepository.existsByUsername(username)) {
            throw LoginFailedException("$username: 존재하지 않는 회원입니다")
        }
    }

    @Transactional
    fun register(registerRequest: RegisterRequest, provider: String) {
        val socialType = SocialType.getValue(provider)
        val username = firebaseTokenParser.getUsername(registerRequest.idToken)
        validateRegisterInformation(username, registerRequest.nickname)
        userRepository.save(createUser(username, socialType, registerRequest))
    }

    fun validateNickname(nickname: String) {
        if (userRepository.existsByNickname(nickname)) {
            throw ExistResourceException("$nickname: 이미 존재하는 닉네임입니다")
        }
    }

    private fun validateRegisterInformation(username: String, nickname: String) {
        if (userRepository.existsByUsername(username)) {
            throw ExistResourceException("$username: 이미 존재하는 회원입니다")
        }
        validateNickname(nickname)
    }

    private fun createUser(
        username: String,
        socialType: SocialType,
        registerRequest: RegisterRequest
    ) = User(
        username = username,
        email = registerRequest.email?.trim(),
        nickname = registerRequest.nickname.trim(),
        phoneNumber = registerRequest.phoneNumber?.trim(),
        gender = registerRequest.gender,
        birthday = registerRequest.birthday,
        socialType = socialType
    )
}
