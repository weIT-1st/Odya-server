package kr.weit.odya.support

import kr.weit.odya.domain.user.Gender
import kr.weit.odya.domain.user.Profile
import kr.weit.odya.domain.user.SocialType
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRole
import kr.weit.odya.domain.user.UsersDocument
import kr.weit.odya.service.dto.FCMTokenRequest
import kr.weit.odya.service.dto.ImageResponse
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.LifeShotRequest
import kr.weit.odya.service.dto.SearchPhoneNumberRequest
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.UserResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import kr.weit.odya.service.dto.UserStatisticsResponse
import java.time.LocalDate

const val TEST_USER_ID = 1L
const val TEST_OTHER_USER_ID = 2L
const val TEST_ANOTHER_USER_ID = 3L
const val TEST_NOT_EXIST_USER_ID = 4L
const val TEST_INVALID_USER_ID = -1L
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
val TEST_GENDER: Gender = Gender.M
val TEST_BIRTHDAY: LocalDate = LocalDate.of(1999, 10, 10)
val TEST_SOCIAL_TYPE: SocialType = SocialType.KAKAO
val TEST_USER_ROLE = UserRole.ROLE_USER
val TEST_USER = createUser()
val TEST_OTHER_USER = createOtherUser()

fun createUser(profileName: String = TEST_DEFAULT_PROFILE_PNG, id: Long = TEST_USER_ID): User = User(
    id = id,
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
    nickname = TEST_OTHER_NICKNAME,
    phoneNumber = TEST_OTHER_PHONE_NUMBER,
    gender = TEST_GENDER,
    birthday = TEST_BIRTHDAY,
    socialType = TEST_SOCIAL_TYPE,
    profile = createProfile(),
)

fun createCustomUser(
    username: String = TEST_OTHER_USERNAME,
    nickname: String = TEST_OTHER_NICKNAME,
) = User(
    id = 0L,
    username,
    null,
    nickname,
    null,
    gender = TEST_GENDER,
    birthday = TEST_BIRTHDAY,
    socialType = TEST_SOCIAL_TYPE,
    profile = createProfile(),
)

fun createUserResponse(): UserResponse = UserResponse(createUser(), TEST_PROFILE_URL)

fun createInformationRequest(): InformationRequest = InformationRequest(TEST_NICKNAME)

fun createFcmTokenRequest(fcmToken: String = TEST_FCM_TOKEN): FCMTokenRequest = FCMTokenRequest(fcmToken)

fun createProfile(profileId: Long = 0L, profileName: String = TEST_DEFAULT_PROFILE_PNG): Profile = Profile(
    id = profileId,
    profileName = profileName,
    profileColor = createProfileColor(TEST_PROFILE_COLOR_ID),
)

fun createUsersDocument(user: User = createUser()): UsersDocument = UsersDocument(user)

fun createSimpleUserResponse(
    user: User = createUser(),
    profileUrl: String = TEST_PROFILE_URL,
    isFollowing: Boolean = false,
): UserSimpleResponse =
    UserSimpleResponse(
        user = user,
        profileUrl = profileUrl,
        isFollowing = isFollowing,
    )

fun createOtherSimpleUserResponse(
    user: User = createOtherUser(),
    profileUrl: String = TEST_PROFILE_URL,
    isFollowing: Boolean = false,
): UserSimpleResponse =
    UserSimpleResponse(
        user = user,
        profileUrl = profileUrl,
        isFollowing = isFollowing,
    )

fun createSliceSimpleUserResponse() = SliceResponse(
    hasNext = false,
    content = listOf(
        createSimpleUserResponse(),
    ),
)

fun createSimpleUserResponseList() = listOf(
    createSimpleUserResponse(),
)

fun createLifeShotImageResponse() = ImageResponse(
    imageId = TEST_IMAGE_ID,
    imageUrl = TEST_IMAGE_URL,
    placeId = TEST_PLACE_ID,
    isLifeShot = true,
    placeName = TEST_PLACE_NAME,
    journalId = TEST_TRAVEL_JOURNAL_ID,
    communityId = null,
)

fun createSliceLifeShotImageResponse() = SliceResponse(
    hasNext = false,
    content = listOf(
        createLifeShotImageResponse(),
    ),
)

fun createUserStatisticsResponse() = UserStatisticsResponse(
    travelJournalCount = TEST_TRAVEL_JOURNAL_COUNT,
    travelPlaceCount = TEST_TRAVEL_PLACE_COUNT,
    followingsCount = TEST_FOLLOWING_COUNT,
    followersCount = TEST_FOLLOWER_COUNT,
    odyaCount = TEST_COMMUNITY_LIKE_COUNT,
    lifeShotCount = TEST_LIFE_SHOT_COUNT,
)

fun createLifeShotRequest(placeName: String? = TEST_PLACE_NAME) = LifeShotRequest(
    placeName = placeName,
)

fun createPhoneNumberList() = listOf(
    SearchPhoneNumberRequest(TEST_PHONE_NUMBER),
)
