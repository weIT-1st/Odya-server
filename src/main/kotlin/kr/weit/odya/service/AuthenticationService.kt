package kr.weit.odya.service

import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.service.dto.AppleRegisterRequest
import kr.weit.odya.service.dto.KakaoLoginRequest
import kr.weit.odya.service.dto.KakaoRegisterRequest
import kr.weit.odya.service.dto.KakaoUserInfo
import kr.weit.odya.service.dto.RegisterRequest
import kr.weit.odya.service.dto.TokenResponse
import kr.weit.odya.support.client.WebClientHelper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

const val HTTPS_KAPI_KAKAO_COM_V_2_USER_ME = "https://kapi.kakao.com/v2/user/me"

private const val OAUTH_ACCESS_TOKEN_TYPE = "BEARER"

@Service
class AuthenticationService(
    private val userRepository: UserRepository,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val webClientHelper: WebClientHelper
) {
    fun appleLoginProcess(appleUsername: String) {
        if (!userRepository.existsByUsername(appleUsername)) {
            throw LoginFailedException("$appleUsername: 존재하지 않는 회원입니다")
        }
    }

    fun kakaoLoginProcess(kakaoUserInfo: KakaoUserInfo): TokenResponse {
        if (!userRepository.existsByUsername(kakaoUserInfo.uid)) {
            throw LoginFailedException("${kakaoUserInfo.uid}: 존재하지 않는 회원입니다")
        }
        return TokenResponse(firebaseTokenHelper.createFirebaseCustomToken(kakaoUserInfo.uid))
    }

    @Transactional
    fun appleRegister(appleRegisterRequest: AppleRegisterRequest) {
        val uid = firebaseTokenHelper.getUid(appleRegisterRequest.idToken)
        appleRegisterRequest.updateUid(uid)
        validateRegisterInformation(appleRegisterRequest)
        userRepository.save(createUser(appleRegisterRequest))
    }

    @Transactional
    fun kakaoRegister(kakaoRegisterRequest: KakaoRegisterRequest) {
        firebaseTokenHelper.createFirebaseUser(kakaoRegisterRequest.uid)
        validateRegisterInformation(kakaoRegisterRequest)
        userRepository.save(createUser(kakaoRegisterRequest))
    }

    fun getUsernameByIdToken(idToken: String): String =
        firebaseTokenHelper.getUid(idToken)

    fun getKakaoUserInfo(kakaoLoginRequest: KakaoLoginRequest): KakaoUserInfo =
        webClientHelper.getWithHeader(HTTPS_KAPI_KAKAO_COM_V_2_USER_ME, KakaoUserInfo::class.java) {
            it.contentType = MediaType.APPLICATION_FORM_URLENCODED
            it.set(HttpHeaders.AUTHORIZATION, getBearerToken(kakaoLoginRequest.accessToken))
        }

    fun validateNickname(nickname: String) {
        if (userRepository.existsByNickname(nickname)) {
            throw ExistResourceException("$nickname: 이미 존재하는 닉네임입니다")
        }
    }

    private fun getBearerToken(oAuthAccessToken: String) = "$OAUTH_ACCESS_TOKEN_TYPE $oAuthAccessToken"

    private fun validateRegisterInformation(registerRequest: RegisterRequest) {
        if (userRepository.existsByUsername(registerRequest.uid)) {
            throw ExistResourceException("${registerRequest.uid}: 이미 존재하는 회원입니다")
        }

        registerRequest.apply {
            email?.apply { if (userRepository.existsByEmail(this)) throw ExistResourceException("$this: 이미 존재하는 이메일입니다") }
            phoneNumber?.apply { if (userRepository.existsByPhoneNumber(this)) throw ExistResourceException("$this: 이미 존재하는 전화번호입니다") }
            validateNickname(nickname)
        }
    }

    private fun createUser(
        registerRequest: RegisterRequest
    ) = User(
        username = registerRequest.uid,
        email = registerRequest.email?.trim(),
        nickname = registerRequest.nickname.trim(),
        phoneNumber = registerRequest.phoneNumber?.trim(),
        gender = registerRequest.gender,
        birthday = registerRequest.birthday,
        socialType = registerRequest.socialType
    )
}
