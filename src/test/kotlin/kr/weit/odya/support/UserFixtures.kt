package kr.weit.odya.support

import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.Profile
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRole
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.UserResponse
import org.springframework.mock.web.MockMultipartFile
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
const val TEST_OTHER_NICKNAME = "testOtherNickname"
const val TEST_PHONE_NUMBER = "010-1234-1234"
const val TEST_OTHER_PHONE_NUMBER = "010-1234-1235"
const val TEST_INVALID_PHONE_NUMBER = "01012341234"
const val TEST_PROFILE_ID = 1L
const val TEST_PROFILE_URL: String = "testProfileUrl"
const val TEST_PROFILE_CONTENT_TYPE = "image/png"
const val TEST_DEFAULT_PROFILE_NAME = "default_profile"
const val TEST_DEFAULT_PROFILE_PNG = "default_profile.png"
const val TEST_INVALID_PROFILE_ORIGINAL_NAME = "default_profile.invalid"
const val TEST_PROFILE_PNG = "example.png"
val TEST_PROFILE_CONTENT_BYTE_ARRAY = "example".byteInputStream()
const val TEST_MOCK_PROFILE_NAME = "profile"
val TEST_GENDER: Gender = Gender.M
val TEST_BIRTHDAY: LocalDate = LocalDate.of(1999, 10, 10)
val TEST_SOCIAL_TYPE: SocialType = SocialType.KAKAO
val TEST_USER_ROLE = UserRole.ROLE_USER
const val TEST_OTHER_USER_ID = 2L

fun createUser(profileName: String = TEST_DEFAULT_PROFILE_PNG): User = User(
    id = TEST_USER_ID,
    username = TEST_USERNAME,
    email = TEST_EMAIL,
    nickname = TEST_NICKNAME,
    phoneNumber = TEST_PHONE_NUMBER,
    gender = TEST_GENDER,
    birthday = TEST_BIRTHDAY,
    socialType = TEST_SOCIAL_TYPE,
    profile = createProfile(TEST_PROFILE_ID, profileName),
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
    profile = createProfile(TEST_PROFILE_ID),
)

fun createUserResponse(): UserResponse = UserResponse(createUser(), TEST_PROFILE_URL)

fun createInformationRequest(): InformationRequest = InformationRequest(TEST_NICKNAME)

fun createProfile(profileId: Long = 0L, profileName: String = TEST_DEFAULT_PROFILE_PNG): Profile = Profile(
    id = profileId,
    profileName = profileName,
    profileColor = createProfileColor(TEST_PROFILE_COLOR_ID),
)

fun createMockProfile(
    name: String = TEST_MOCK_PROFILE_NAME,
    originalFileName: String? = TEST_DEFAULT_PROFILE_PNG,
    contentType: String? = TEST_PROFILE_CONTENT_TYPE,
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        TEST_PROFILE_CONTENT_BYTE_ARRAY,
    )
}
