package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getAllByUserId
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByUserId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocument
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.domain.user.getByNickname
import kr.weit.odya.domain.user.getByPhoneNumbers
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIdWithProfile
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.security.InvalidTokenException
import kr.weit.odya.support.DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_NAME
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_FCM_TOKEN
import kr.weit.odya.support.TEST_FOLLOWER_COUNT
import kr.weit.odya.support.TEST_FOLLOWING_COUNT
import kr.weit.odya.support.TEST_ID_TOKEN
import kr.weit.odya.support.TEST_INVALID_PROFILE_ORIGINAL_NAME
import kr.weit.odya.support.TEST_LIFE_SHOT_COUNT
import kr.weit.odya.support.TEST_MOCK_PROFILE_NAME
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_PROFILE_URL
import kr.weit.odya.support.TEST_PROFILE_WEBP
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createFcmTokenRequest
import kr.weit.odya.support.createInformationRequest
import kr.weit.odya.support.createMockProfile
import kr.weit.odya.support.createNoneProfileColor
import kr.weit.odya.support.createProfileColor
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createUser
import kr.weit.odya.support.createUserResponse
import kr.weit.odya.support.createUserStatisticsResponse
import kr.weit.odya.support.createUsersDocument

class UserServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val firebaseTokenHelper = mockk<FirebaseTokenHelper>()
        val fileService = mockk<FileService>()
        val profileColorService = mockk<ProfileColorService>()
        val usersDocumentRepository = mockk<UsersDocumentRepository>()
        val followRepository = mockk<FollowRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val communityRepository = mockk<CommunityRepository>()
        val contentImageRepository = mockk<ContentImageRepository>()
        val userService =
            UserService(
                userRepository,
                firebaseTokenHelper,
                fileService,
                profileColorService,
                usersDocumentRepository,
                followRepository,
                travelJournalRepository,
                communityRepository,
                contentImageRepository,
            )

        describe("getInformation") {
            context("가입되어 있는 USER ID가 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser()
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("UserResponse를 반환한다") {
                    val userResponse = userService.getInformation(TEST_USER_ID)
                    userResponse shouldBe createUserResponse()
                }
            }

            context("preAuthentication Access Url 생성에 실패한 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser()
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> { userService.getInformation(TEST_USER_ID) }
                }
            }

            context("가입되어 있지 않은 USER ID가 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { userService.getInformation(TEST_USER_ID) }
                }
            }
        }

        describe("getEmailByIdToken") {
            context("유효하고 인증된 이메일이 있는 토큰이 주어지는 경우") {
                every { firebaseTokenHelper.getEmail(TEST_ID_TOKEN) } returns TEST_EMAIL
                it("이메일을 반환한다") {
                    val response = userService.getEmailByIdToken(TEST_ID_TOKEN)
                    response shouldBe TEST_EMAIL
                }
            }

            context("유효하지만 인증된 이메일이 없는 토큰이 주어지는 경우") {
                every { firebaseTokenHelper.getEmail(TEST_ID_TOKEN) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> { userService.getEmailByIdToken(TEST_ID_TOKEN) }
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                every { firebaseTokenHelper.getEmail(TEST_ID_TOKEN) } throws InvalidTokenException()
                it("[InvalidTokenException] 반환한다") {
                    shouldThrow<InvalidTokenException> { userService.getEmailByIdToken(TEST_ID_TOKEN) }
                }
            }
        }

        describe("getPhoneNumberByIdToken") {
            context("유효하고 인증된 전화번호가 있는 토큰이 주어지는 경우") {
                every { firebaseTokenHelper.getPhoneNumber(TEST_ID_TOKEN) } returns TEST_PHONE_NUMBER
                it("전화번호를 반환한다") {
                    val response = userService.getPhoneNumberByIdToken(TEST_ID_TOKEN)
                    response shouldBe TEST_PHONE_NUMBER
                }
            }

            context("유효하지만 인증된 전화번호가 없는 토큰이 주어지는 경우") {
                every { firebaseTokenHelper.getPhoneNumber(TEST_ID_TOKEN) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) }
                }
            }

            context("유효하지 않은 토큰이 주어지는 경우") {
                every { firebaseTokenHelper.getPhoneNumber(TEST_ID_TOKEN) } throws InvalidTokenException()
                it("[InvalidTokenException] 반환한다") {
                    shouldThrow<InvalidTokenException> { userService.getPhoneNumberByIdToken(TEST_ID_TOKEN) }
                }
            }
        }

        describe("updateEmail") {
            context("가입되어 있는 USER ID와 유효한 이메일이 주어지는 경우") {
                every { userRepository.existsByEmail(TEST_EMAIL) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) }
                }
            }

            context("이미 존재하는 이메일인 경우") {
                every { userRepository.existsByEmail(TEST_EMAIL) } returns true
                it("[ExistResourceException] 반환한다") {
                    shouldThrow<ExistResourceException> { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) }
                }
            }

            context("유효한 이메일이지만, 가입되어 있지 않은 USER ID인 경우") {
                every { userRepository.existsByEmail(TEST_EMAIL) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> { userService.updateEmail(TEST_USER_ID, TEST_EMAIL) }
                }
            }
        }

        describe("updatePhoneNumber") {
            context("가입되어 있는 USER ID와 유효한 전화번호가 주어지는 경우") {
                every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER) }
                }
            }

            context("이미 존재하는 전화번호인 경우") {
                every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns true
                it("[ExistResourceException] 반환한다") {
                    shouldThrow<ExistResourceException> {
                        userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER)
                    }
                }
            }

            context("유효한 전화번호가 주어지지만, 가입되어 있지 않은 USER ID인 경우") {
                every { userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        userService.updatePhoneNumber(TEST_USER_ID, TEST_PHONE_NUMBER)
                    }
                }
            }
        }

        describe("updateInformation") {
            val informationRequest = createInformationRequest()
            context("가입되어 있는 USER ID와 유효한 정보 요청이 주어지는 경우") {
                every { userRepository.existsByNickname(TEST_NICKNAME) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { usersDocumentRepository.save(any()) } returns UsersDocument(TEST_USER_ID, TEST_NICKNAME)
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.updateInformation(TEST_USER_ID, informationRequest) }
                }
            }

            context("이미 존재하는 닉네임이 주어지는 경우") {
                every { userRepository.existsByNickname(TEST_NICKNAME) } returns true
                it("[ExistResourceException] 반환한다") {
                    shouldThrow<ExistResourceException> {
                        userService.updateInformation(TEST_USER_ID, informationRequest)
                    }
                }
            }

            context("유효한 정보 요청이지만, 가입되어 있지 않은 USER ID인 경우") {
                every { userRepository.existsByNickname(TEST_NICKNAME) } returns false
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        userService.updateInformation(TEST_USER_ID, informationRequest)
                    }
                }
            }
        }

        describe("uploadProfile") {
            context("유효한 프로필 INPUT STREAM과 ORIGINAL FILE NAME이 주어지는 경우") {
                val mockFile = createMockProfile()
                every { fileService.saveFile(mockFile) } returns TEST_DEFAULT_PROFILE_NAME
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.uploadProfile(mockFile) }
                }
            }

            context("올바르지 않은 형식의 ORIGINAL FILE NAME이 주어지는 경우") {
                val mockFile = createMockProfile(originalFileName = TEST_INVALID_PROFILE_ORIGINAL_NAME)
                every { fileService.saveFile(mockFile) } throws IllegalArgumentException("프로필 사진은 ${ALLOW_FILE_FORMAT_LIST.joinToString()} 형식만 가능합니다")
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        userService.uploadProfile(mockFile)
                    }
                }
            }

            context("ORIGINAL FILE NAME이 주어지지 않는 경우") {
                val mockFile = createMockProfile(originalFileName = null)
                every { fileService.saveFile(mockFile) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        userService.uploadProfile(mockFile)
                    }
                }
            }

            context("프로필 업로드에 실패하는 경우") {
                val mockFile = createMockProfile()
                every { fileService.saveFile(mockFile) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> {
                        userService.uploadProfile(mockFile)
                    }
                }
            }
        }

        describe("deleteProfile") {
            context("유효한 USER ID가 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser(TEST_PROFILE_WEBP)
                every { fileService.deleteFile(TEST_PROFILE_WEBP) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.deleteProfile(TEST_USER_ID) }
                }
            }

            context("사용자의 프로필이 기본 이미지일 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        userService.deleteProfile(TEST_USER_ID)
                    }
                }
            }

            context("OBJECT STORAGE에 프로필이 없어 프로필 삭제에 실패하는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser(TEST_PROFILE_WEBP)
                every { fileService.deleteFile(TEST_PROFILE_WEBP) } throws IllegalArgumentException(
                    DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE,
                )
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> { userService.deleteProfile(TEST_USER_ID) }
                }
            }

            context("프로필 업로드에 실패하는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser(TEST_PROFILE_WEBP)
                every { fileService.deleteFile(TEST_PROFILE_WEBP) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> { userService.deleteProfile(TEST_USER_ID) }
                }
            }
        }

        describe("updateProfile") {
            context("유효한 USER ID, PROFILE NAME, ORIGINAL FILE NAME이 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser()
                every { profileColorService.getNoneProfileColor() } returns createNoneProfileColor()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.updateProfile(TEST_USER_ID, TEST_PROFILE_WEBP, TEST_PROFILE_WEBP) }
                }
            }

            context("유효한 USER ID와 NULL 값인 PROFILE NAME, ORIGINAL FILE NAME이 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser()
                every { profileColorService.getRandomProfileColor() } returns createProfileColor()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.updateProfile(TEST_USER_ID, null, null) }
                }
            }

            context("유효하지 않은 USER ID가 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        userService.updateProfile(TEST_USER_ID, TEST_PROFILE_WEBP, TEST_PROFILE_WEBP)
                    }
                }
            }
        }

        describe("searchByNickname") {
            context("유효한 nickname이 주어지면") {
                val user = createUser()
                every { usersDocumentRepository.getByNickname(TEST_NICKNAME) } returns listOf(createUsersDocument(user))
                every { userRepository.getByUserIds(any(), any(), any()) } returns listOf(user)
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                it("유저를 조회 한다") {
                    shouldNotThrowAny { userService.searchByNickname(TEST_USER_ID, TEST_NICKNAME, 10, null) }
                }
            }
        }

        describe("getStatistics") {
            context("가입된 USER ID 정보가 주어지는 경우") {
                every { followRepository.countByFollowerId(TEST_USER_ID) } returns TEST_FOLLOWING_COUNT
                every { followRepository.countByFollowingId(TEST_USER_ID) } returns TEST_FOLLOWER_COUNT
                every { travelJournalRepository.getByUserId(TEST_USER_ID) } returns listOf(createTravelJournal())
                every { communityRepository.getAllByUserId(TEST_USER_ID) } returns listOf(createCommunity()).onEach { community ->
                    repeat(2) { community.increaseLikeCount() }
                }
                every { contentImageRepository.countByUserIdAndIsLifeShotIsTrue(TEST_USER_ID) } returns TEST_LIFE_SHOT_COUNT
                it("[FollowCountsResponse] 반환한다.") {
                    val response = userService.getStatistics(TEST_USER_ID)
                    response shouldBe createUserStatisticsResponse()
                }
            }
        }

        describe("updateFcmToken") {
            context("유효한 USER ID와 FCM TOKEN이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { userRepository.findByFcmToken(TEST_FCM_TOKEN) } returns null
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.updateFcmToken(TEST_USER_ID, createFcmTokenRequest()) }
                }
            }
        }

        describe("deleteUserRelatedData") {
            context("가입된 USER ID 정보가 주어지는 경우(프로필 Default X)") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser(TEST_MOCK_PROFILE_NAME)
                every { followRepository.deleteByFollowingId(TEST_USER_ID) } just runs
                every { followRepository.deleteByFollowerId(TEST_USER_ID) } just runs
                every { userRepository.deleteById(TEST_USER_ID) } just runs
                every { usersDocumentRepository.deleteById(TEST_USER_ID) } just runs
                every { fileService.deleteFile(TEST_MOCK_PROFILE_NAME) } just runs
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny { userService.deleteUserRelatedData(TEST_USER_ID) }
                }
            }

            context("가입된 USER ID 정보가 주어지는 경우(프로필 Default)") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } returns createUser()
                every { followRepository.deleteByFollowingId(TEST_USER_ID) } just runs
                every { followRepository.deleteByFollowerId(TEST_USER_ID) } just runs
                every { userRepository.deleteById(TEST_USER_ID) } just runs
                every { usersDocumentRepository.deleteById(TEST_USER_ID) } just runs
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny { userService.deleteUserRelatedData(TEST_USER_ID) }
                }
            }

            context("가입되지 않은 USER ID 정보가 주어지는 경우") {
                every { userRepository.getByUserIdWithProfile(TEST_USER_ID) } throws NoSuchElementException()
                it("[NosuchElementException]을 반환한다") {
                    shouldThrow<NoSuchElementException> { userService.deleteUserRelatedData(TEST_USER_ID) }
                }
            }
        }

        describe("searchByPhoneNumbers") {
            context("유효한 USER ID와 PHONE NUMBER가 주어지는 경우") {
                every { userRepository.getByPhoneNumbers(any()) } returns listOf(createUser())
                every { followRepository.getFollowingIds(TEST_USER_ID) } returns listOf(TEST_USER_ID)
                every { fileService.getPreAuthenticatedObjectUrl(TEST_DEFAULT_PROFILE_PNG) } returns TEST_PROFILE_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { userService.searchByPhoneNumbers(TEST_USER_ID, listOf()) }
                }
            }
        }
    },
)
