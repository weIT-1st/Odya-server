package kr.weit.odya.service

import kr.weit.odya.client.KakaoClientException
import kr.weit.odya.client.kakao.KakaoClient
import kr.weit.odya.client.kakao.KakaoUserInfo
import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.domain.user.Profile
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocument
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.service.dto.KakaoLoginRequest
import kr.weit.odya.service.dto.RegisterRequest
import kr.weit.odya.service.dto.TokenResponse
import kr.weit.odya.util.getOrThrow
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val OAUTH_ACCESS_TOKEN_TYPE = "BEARER"

@Service
class AuthenticationService(
    private val termsService: TermsService,
    private val userRepository: UserRepository,
    private val profileColorService: ProfileColorService,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val kakaoClient: KakaoClient,
    private val usersDocumentRepository: UsersDocumentRepository,
) {
    fun appleLoginProcess(appleUsername: String) {
        if (!userRepository.existsByUsername(appleUsername)) {
            throw UnRegisteredUserException("$appleUsername: 존재하지 않는 회원입니다")
        }
    }

    fun kakaoLoginProcess(kakaoUserInfo: KakaoUserInfo): TokenResponse {
        if (!userRepository.existsByUsername(kakaoUserInfo.username)) {
            throw UnRegisteredUserException("${kakaoUserInfo.username}: 존재하지 않는 회원입니다")
        }
        return TokenResponse(firebaseTokenHelper.createFirebaseCustomToken(kakaoUserInfo.username))
    }

    @Transactional
    fun register(registerRequest: RegisterRequest) {
        val termsIdList = registerRequest.termsIdList
        termsService.checkRequiredTerms(termsIdList)
        validateRegisterInformation(registerRequest)
        val randomProfileColor = profileColorService.getRandomProfileColor()
        val user = userRepository.save(createUser(registerRequest, randomProfileColor))
        if (registerRequest.socialType == SocialType.KAKAO) {
            firebaseTokenHelper.createFirebaseUser(registerRequest.username)
        }
        termsService.saveAllAgreedTerms(user, termsIdList)
        usersDocumentRepository.save(UsersDocument(user))
    }

    fun validateNickname(nickname: String) {
        if (userRepository.existsByNickname(nickname)) {
            throw ExistResourceException("$nickname: 이미 존재하는 닉네임입니다")
        }
    }

    fun validateEmail(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw ExistResourceException("$email: 이미 존재하는 이메일입니다")
        }
    }

    fun validatePhoneNumber(phoneNumber: String) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw ExistResourceException("$phoneNumber: 이미 존재하는 전화번호입니다")
        }
    }

    fun getUsernameByIdToken(idToken: String): String = firebaseTokenHelper.getUid(idToken)

    fun getKakaoUserInfo(kakaoLoginRequest: KakaoLoginRequest): KakaoUserInfo = runCatching {
        kakaoClient.getKakaoUserInfo(getBearerToken(kakaoLoginRequest.accessToken))
    }.getOrThrow { ex -> KakaoClientException(ex.message) }

    private fun getBearerToken(oAuthAccessToken: String) = "$OAUTH_ACCESS_TOKEN_TYPE $oAuthAccessToken"

    private fun validateRegisterInformation(registerRequest: RegisterRequest) {
        if (userRepository.existsByUsername(registerRequest.username)) {
            throw ExistResourceException("${registerRequest.username}: 이미 존재하는 회원입니다")
        }

        registerRequest.apply {
            email?.apply { validateEmail(this) }
            phoneNumber?.apply { validatePhoneNumber(this) }
            validateNickname(nickname)
        }
    }

    private fun createUser(
        registerRequest: RegisterRequest,
        randomProfileColor: ProfileColor,
    ) = User(
        username = registerRequest.username,
        email = registerRequest.email?.trim(),
        nickname = registerRequest.nickname.trim(),
        phoneNumber = registerRequest.phoneNumber?.trim(),
        gender = registerRequest.gender,
        birthday = registerRequest.birthday,
        socialType = registerRequest.socialType,
        profile = Profile(profileColor = randomProfileColor),
    )
}
