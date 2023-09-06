package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_COMMUNITY_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_TOPIC_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityContentImagePairs
import kr.weit.odya.support.createCommunityCreateRequest
import kr.weit.odya.support.createFollowerFcmTokenList
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockImageFiles
import kr.weit.odya.support.createPrivateTravelJournal
import kr.weit.odya.support.createTopic
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createUser
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile

class CommunityServiceTest : DescribeSpec(
    {
        val communityRepository = mockk<CommunityRepository>()
        val topicRepository = mockk<TopicRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val userRepository = mockk<UserRepository>()
        val fileService = mockk<FileService>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val followRepository = mockk<FollowRepository>()
        val communityService =
            CommunityService(
                communityRepository,
                topicRepository,
                travelJournalRepository,
                userRepository,
                fileService,
                applicationEventPublisher,
                followRepository,
            )

        describe("createCommunity") {
            context("유효한 데이터가 주어지는 경우") {
                val communityCreateRequest = createCommunityCreateRequest(
                    placeId = TEST_PLACE_ID,
                    travelJournalId = TEST_TRAVEL_JOURNAL_ID,
                )
                val imageNamePairs = createCommunityContentImagePairs()
                val register = createUser()
                every { userRepository.getByUserId(TEST_USER_ID) } returns register
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                every { topicRepository.getByTopicId(TEST_TOPIC_ID) } returns createTopic()
                every { communityRepository.save(any<Community>()) } returns createCommunity()
                every { followRepository.findFollowerFcmTokenByFollowingId(TEST_USER_ID) } returns createFollowerFcmTokenList()
                every { applicationEventPublisher.publishEvent(any<PushNotificationEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.createCommunity(
                            TEST_USER_ID,
                            communityCreateRequest,
                            imageNamePairs,
                        )
                    }
                }
            }

            context("유효한 데이터가 주어지는 경우(토픽 및 여행 일지 NULL)") {
                val communityCreateRequest = createCommunityCreateRequest()
                val register = createUser()
                val imageNamePairs = createCommunityContentImagePairs()
                every { userRepository.getByUserId(TEST_USER_ID) } returns register
                every { communityRepository.save(any<Community>()) } returns createCommunity()
                every { followRepository.findFollowerFcmTokenByFollowingId(TEST_USER_ID) } returns createFollowerFcmTokenList()
                every { applicationEventPublisher.publishEvent(any<PushNotificationEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.createCommunity(
                            TEST_USER_ID,
                            communityCreateRequest,
                            imageNamePairs,
                        )
                    }
                }
            }

            context("비공개 여행일지를 연결하려고 하는 경우") {
                val communityCreateRequest = createCommunityCreateRequest(travelJournalId = 2L)
                val imageNamePairs = createCommunityContentImagePairs()
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { travelJournalRepository.getByTravelJournalId(2L) } returns createPrivateTravelJournal()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        communityService.createCommunity(
                            TEST_USER_ID,
                            communityCreateRequest,
                            imageNamePairs,
                        )
                    }
                }
            }
        }

        describe("uploadContentImages") {
            context("유효한 데이터가 주어지는 경우") {
                val mockImageFiles = createMockImageFiles(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME)
                every { fileService.saveFile(any<MultipartFile>()) } returns TEST_GENERATED_FILE_NAME
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.uploadContentImages(mockImageFiles)
                    }
                }
            }

            context("이미지 파일의 원본 이름이 없는 경우") {
                val mockImageFiles =
                    listOf(createMockImageFile(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME, originalFileName = null))
                every { fileService.saveFile(any<MultipartFile>()) } throws IllegalArgumentException(
                    "파일 원본 이름은 필수 값입니다.",
                )
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        communityService.uploadContentImages(mockImageFiles)
                    }
                }
            }

            context("이미지 파일 업로드에 실패하는 경우") {
                val mockImageFiles = createMockImageFiles(mockFileName = TEST_COMMUNITY_MOCK_FILE_NAME)
                every { fileService.saveFile(any<MultipartFile>()) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> {
                        communityService.uploadContentImages(mockImageFiles)
                    }
                }
            }
        }
    },
)
