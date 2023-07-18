package kr.weit.odya.support

import kr.weit.odya.client.kakao.KakaoUserInfo
import kr.weit.odya.service.dto.AppleLoginRequest
import kr.weit.odya.service.dto.AppleRegisterRequest
import kr.weit.odya.service.dto.KakaoLoginRequest
import kr.weit.odya.service.dto.KakaoRegisterErrorResponse
import kr.weit.odya.service.dto.KakaoRegisterRequest
import kr.weit.odya.service.dto.TokenResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

const val TEST_ID_TOKEN = "testIdToken"

const val TEST_CUSTOM_TOKEN = "testCustomToken"

const val TEST_OAUTH_ACCESS_TOKEN = "oAuthAccessToken"

const val TEST_OAUTH_ID = "1234"

const val TEST_KAKAO_UID = "KAKAO_1234"

const val TEST_NOT_EXIST_USER_ID_TOKEN = "testNotExistUserToken"

const val TEST_INVALID_ID_TOKEN = "testInvalidIdToken"

const val TEST_BEARER_ID_TOKEN = "Bearer $TEST_ID_TOKEN"

const val TEST_BEARER_NOT_EXIST_USER_ID_TOKEN = "Bearer $TEST_NOT_EXIST_USER_ID_TOKEN"

const val TEST_BEARER_INVALID_ID_TOKEN = "Bearer $TEST_INVALID_ID_TOKEN"

const val TEST_BEARER_OAUTH_ACCESS_TOKEN = "BEARER oAuthAccessToken"

fun createAppleLoginRequest() = AppleLoginRequest(TEST_ID_TOKEN)

fun createKakaoLoginRequest() = KakaoLoginRequest(TEST_OAUTH_ACCESS_TOKEN)

fun createKakaoUserInfo() =
    KakaoUserInfo(TEST_OAUTH_ID, KakaoUserInfo.KakaoAccount(profile = KakaoUserInfo.Profile(TEST_NICKNAME)))

fun createKakaoRegisterErrorResponse(kakaoUserInfo: KakaoUserInfo) = KakaoRegisterErrorResponse(kakaoUserInfo)

fun createTokenResponse() = TokenResponse(TEST_CUSTOM_TOKEN)

fun createAppleRegisterRequest() =
    AppleRegisterRequest(TEST_ID_TOKEN, TEST_EMAIL, TEST_PHONE_NUMBER, TEST_NICKNAME, TEST_GENDER, TEST_BIRTHDAY)

fun createKakaoRegisterRequest() =
    KakaoRegisterRequest(TEST_KAKAO_UID, TEST_EMAIL, TEST_PHONE_NUMBER, TEST_NICKNAME, TEST_GENDER, TEST_BIRTHDAY)

fun userAuthentication() =
    UsernamePasswordAuthenticationToken(TEST_USER_ID, null, listOf(SimpleGrantedAuthority(TEST_USER_ROLE.name)))
