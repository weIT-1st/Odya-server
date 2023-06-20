package kr.weit.odya.support

import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.UserResponse
import java.time.LocalDate

const val TEST_USER_ID = 1L
const val TEST_INVALID_USER_ID = -1L
const val TEST_NOT_EXIST_USER_ID = 2L
const val TEST_USERNAME = "testUsername"
const val TEST_OTHER_USERNAME = "testOtherUsername"
const val TEST_EMAIL = "test@test.com"
const val TEST_OTHER_EMAIL = "other@test.com"
const val TEST_INVALID_EMAIL = "test"
const val TEST_NICKNAME = "testNickname"
const val TEST_PHONE_NUMBER = "010-1234-1234"
const val TEST_OTHER_PHONE_NUMBER = "010-1234-1235"
const val TEST_INVALID_PHONE_NUMBER = "01012341234"
const val TEST_PROFILE_NAME: String = "testProfileName"
val TEST_GENDER: Gender = Gender.M
val TEST_BIRTHDAY: LocalDate = LocalDate.of(1999, 10, 10)
val TEST_SOCIAL_TYPE: SocialType = SocialType.KAKAO

const val TEST_OTHER_USER_ID = 2L

fun createUser(): User = User(
    id = TEST_USER_ID,
    username = TEST_USERNAME,
    email = TEST_EMAIL,
    nickname = TEST_NICKNAME,
    phoneNumber = TEST_PHONE_NUMBER,
    gender = TEST_GENDER,
    birthday = TEST_BIRTHDAY,
    socialType = TEST_SOCIAL_TYPE,
    profileName = TEST_PROFILE_NAME
)

fun createOtherUser(): User = User(
    id = TEST_OTHER_USER_ID,
    username = TEST_OTHER_USERNAME,
    email = TEST_OTHER_EMAIL,
    nickname = TEST_OTHER_USERNAME,
    phoneNumber = TEST_OTHER_PHONE_NUMBER,
    gender = TEST_GENDER,
    birthday = TEST_BIRTHDAY,
    socialType = TEST_SOCIAL_TYPE,
    profileName = TEST_PROFILE_NAME
)

fun createUserResponse(): UserResponse = UserResponse(createUser())

fun createInformationRequest(): InformationRequest = InformationRequest(TEST_NICKNAME)
