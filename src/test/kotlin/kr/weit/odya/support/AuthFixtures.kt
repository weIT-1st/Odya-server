package kr.weit.odya.support

import kr.weit.odya.service.dto.LoginRequest
import kr.weit.odya.service.dto.RegisterRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

const val TEST_ID_TOKEN = "testIdToken"

const val TEST_NOT_EXIST_USER_ID_TOKEN = "testNotExistUserToken"

const val TEST_INVALID_ID_TOKEN = "testInvalidIdToken"

const val TEST_BEARER_ID_TOKEN = "Bearer $TEST_ID_TOKEN"

const val TEST_BEARER_NOT_EXIST_USER_ID_TOKEN = "Bearer $TEST_NOT_EXIST_USER_ID_TOKEN"

const val TEST_BEARER_INVALID_ID_TOKEN = "Bearer $TEST_INVALID_ID_TOKEN"

const val SOMETHING_ERROR_MESSAGE = "something error message"

const val TEST_PROVIDER = "kakao"

fun createLoginRequest() = LoginRequest(TEST_ID_TOKEN)

fun createRegisterRequest() =
    RegisterRequest(TEST_ID_TOKEN, TEST_EMAIL, TEST_PHONE_NUMBER, TEST_NICKNAME, TEST_GENDER, TEST_BIRTHDAY)

fun userAuthentication() =
    UsernamePasswordAuthenticationToken(TEST_USER_ID, null, listOf(SimpleGrantedAuthority("ROLE_USER")))
