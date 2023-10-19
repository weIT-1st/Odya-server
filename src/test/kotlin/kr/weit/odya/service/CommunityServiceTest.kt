package kr.weit.odya.service

import com.google.maps.errors.InvalidRequestException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.client.GoogleMapsClient
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityDeleteEvent
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.CommunitySortType
import kr.weit.odya.domain.community.CommunityUpdateEvent
import kr.weit.odya.domain.community.CommunityVisibility
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.community.getCommunityByTopic
import kr.weit.odya.domain.community.getCommunitySliceBy
import kr.weit.odya.domain.community.getFriendCommunitySliceBy
import kr.weit.odya.domain.community.getLikedCommunitySliceBy
import kr.weit.odya.domain.community.getMyCommunitySliceBy
import kr.weit.odya.domain.communitycomment.CommunityCommentRepository
import kr.weit.odya.domain.communitycomment.deleteCommunityComments
import kr.weit.odya.domain.communitylike.CommunityLikeRepository
import kr.weit.odya.domain.communitylike.deleteCommunityLikes
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.report.ReportCommunityRepository
import kr.weit.odya.domain.report.deleteAllByUserId
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_COMMUNITY_COMMENT_COUNT
import kr.weit.odya.support.TEST_COMMUNITY_CONTENT_IMAGE_DELETE_ID
import kr.weit.odya.support.TEST_COMMUNITY_ID
import kr.weit.odya.support.TEST_COMMUNITY_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
import kr.weit.odya.support.TEST_INVALID_TOPIC_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_TOPIC_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_UPDATE_TOPIC_ID
import kr.weit.odya.support.TEST_UPDATE_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createAllCommunities
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityContentImagePairs
import kr.weit.odya.support.createCommunityContentImageUpdatePairs
import kr.weit.odya.support.createCommunityCreateRequest
import kr.weit.odya.support.createCommunityUpdateRequest
import kr.weit.odya.support.createFollowerFcmTokenList
import kr.weit.odya.support.createFriendCommunities
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockImageFiles
import kr.weit.odya.support.createMyCommunities
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceDetails
import kr.weit.odya.support.createPrivateTravelJournal
import kr.weit.odya.support.createTopic
import kr.weit.odya.support.createTopicCommunities
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createUser
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile

class CommunityServiceTest : DescribeSpec(
    {
        val communityRepository = mockk<CommunityRepository>()
        val communityCommentRepository = mockk<CommunityCommentRepository>()
        val communityLikeRepository = mockk<CommunityLikeRepository>()
        val topicRepository = mockk<TopicRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val userRepository = mockk<UserRepository>()
        val fileService = mockk<FileService>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val followRepository = mockk<FollowRepository>()
        val googleMapsClient = mockk<GoogleMapsClient>()
        val reportCommunityRepository = mockk<ReportCommunityRepository>()
        val communityService =
            CommunityService(
                communityRepository,
                communityCommentRepository,
                communityLikeRepository,
                topicRepository,
                travelJournalRepository,
                userRepository,
                fileService,
                applicationEventPublisher,
                followRepository,
                googleMapsClient,
                reportCommunityRepository,
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
                every { googleMapsClient.findPlaceDetailsByPlaceId(any()) } returns createPlaceDetails()
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

            context("구글 맵에 존재하지 않는 장소 id를 요청했을 경우") {
                val communityCreateRequest = createCommunityCreateRequest(
                    placeId = TEST_PLACE_ID,
                    travelJournalId = TEST_TRAVEL_JOURNAL_ID,
                )
                val imageNamePairs = createCommunityContentImagePairs()
                val register = createUser()
                every { userRepository.getByUserId(TEST_USER_ID) } returns register
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                every { topicRepository.getByTopicId(TEST_TOPIC_ID) } returns createTopic()
                every { googleMapsClient.findPlaceDetailsByPlaceId(TEST_PLACE_ID) } throws InvalidRequestException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[InvalidRequestException] 예외가 발생한다.") {
                    shouldThrow<InvalidRequestException> {
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

            context("타인의 여행일지를 연결하려고 하는 경우") {
                val communityCreateRequest = createCommunityCreateRequest(travelJournalId = 2L)
                val imageNamePairs = createCommunityContentImagePairs()
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { travelJournalRepository.getByTravelJournalId(2L) } returns createTravelJournal(
                    id = 2L,
                    user = createOtherUser(),
                )
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
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

        describe("getCommunity") {
            context("유효한 데이터가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { communityCommentRepository.countByCommunityId(TEST_COMMUNITY_ID) } returns TEST_COMMUNITY_COMMENT_COUNT
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                every { communityCommentRepository.countByCommunityId(TEST_COMMUNITY_ID) } returns TEST_COMMUNITY_COMMENT_COUNT
                every {
                    communityLikeRepository.existsByCommunityIdAndUserId(
                        TEST_COMMUNITY_ID,
                        TEST_USER_ID,
                    )
                } returns false
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.getCommunity(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("존재하지 않은 커뮤니티 아이디가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        communityService.getCommunity(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("작성자와 요청한 사용자가 같지 않을 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity(
                    user = createOtherUser(),
                    visibility = CommunityVisibility.FRIEND_ONLY,
                )
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_OTHER_USER_ID,
                        TEST_USER_ID,
                    )
                } throws ForbiddenException("친구가 아닌 사용자($TEST_USER_ID)는 친구에게만 공개하는 여행 일지($TEST_COMMUNITY_ID)를 조회할 수 없습니다.")
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        communityService.getCommunity(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("Object Storage에 커뮤니티 이미지가 없을 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { fileService.getPreAuthenticatedObjectUrl(any()) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> {
                        communityService.getCommunity(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }
        }

        describe("getCommunities") {
            context("유효한 데이터가 주어지는 경우") {
                every {
                    communityRepository.getCommunitySliceBy(
                        TEST_USER_ID,
                        10,
                        null,
                        CommunitySortType.LATEST,
                    )
                } returns createAllCommunities()
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                every { followRepository.existsByFollowerIdAndFollowingId(any<Long>(), any<Long>()) } returns false
                every { communityCommentRepository.countByCommunityId(any<Long>()) } returns TEST_COMMUNITY_COMMENT_COUNT
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.getCommunities(TEST_USER_ID, 10, null, CommunitySortType.LATEST)
                    }
                }
            }
        }

        describe("getMyCommunities") {
            context("유효한 데이터가 주어지는 경우") {
                every {
                    communityRepository.getMyCommunitySliceBy(
                        TEST_USER_ID,
                        10,
                        null,
                        CommunitySortType.LATEST,
                    )
                } returns createMyCommunities()
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                every { communityCommentRepository.countByCommunityId(any<Long>()) } returns TEST_COMMUNITY_COMMENT_COUNT
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.getMyCommunities(TEST_USER_ID, 10, null, CommunitySortType.LATEST)
                    }
                }
            }
        }

        describe("getFriendCommunities") {
            context("유효한 데이터가 주어지는 경우") {
                every {
                    communityRepository.getFriendCommunitySliceBy(
                        TEST_USER_ID,
                        10,
                        null,
                        CommunitySortType.LATEST,
                    )
                } returns createFriendCommunities()
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                every { followRepository.existsByFollowerIdAndFollowingId(any<Long>(), any<Long>()) } returns false
                every { communityCommentRepository.countByCommunityId(any<Long>()) } returns TEST_COMMUNITY_COMMENT_COUNT
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.getFriendCommunities(TEST_USER_ID, 10, null, CommunitySortType.LATEST)
                    }
                }
            }
        }

        describe("searchByTopic") {
            context("유효한 데이터가 주어지는 경우") {
                val topic = createTopic()
                every { topicRepository.getByTopicId(TEST_TOPIC_ID) } returns topic
                every { communityRepository.getCommunityByTopic(topic, TEST_DEFAULT_SIZE, null, CommunitySortType.LATEST) } returns createTopicCommunities()
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                every { communityCommentRepository.countByCommunityId(any<Long>()) } returns TEST_COMMUNITY_COMMENT_COUNT
                it("정상적으로 종료한다.") {
                    shouldNotThrowAny {
                        communityService.searchByTopic(TEST_USER_ID, TEST_TOPIC_ID, TEST_DEFAULT_SIZE, null, CommunitySortType.LATEST)
                    }
                }
            }

            context("존재하지 않는 토픽 아이디가 주어지는 경우") {
                every { topicRepository.getByTopicId(TEST_INVALID_TOPIC_ID) } throws NoSuchElementException("$TEST_TOPIC_ID: 존재하지 않는 토픽입니다.")
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        communityService.searchByTopic(TEST_USER_ID, TEST_INVALID_TOPIC_ID, 10, null, CommunitySortType.LATEST)
                    }
                }
            }
        }

        describe("getLikedCommunities") {
            context("유효한 데이터가 주어지는 경우") {
                every { communityRepository.getLikedCommunitySliceBy(TEST_USER_ID, 10, null, CommunitySortType.LATEST) } returns createAllCommunities()
                every { fileService.getPreAuthenticatedObjectUrl(any()) } returns TEST_FILE_AUTHENTICATED_URL
                every { communityCommentRepository.countByCommunityId(any<Long>()) } returns TEST_COMMUNITY_COMMENT_COUNT
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { communityService.getLikedCommunities(TEST_USER_ID, 10, null, CommunitySortType.LATEST) }
                }
            }

            context("유효한 데이터가 주어지는 경우") {
                every { communityRepository.getLikedCommunitySliceBy(TEST_USER_ID, 10, null, CommunitySortType.LATEST) } returns createAllCommunities()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { communityService.getLikedCommunities(TEST_USER_ID, 10, null, CommunitySortType.LATEST) }
                }
            }
        }

        describe("validateUpdateCommunityRequest") {
            context("유효한 데이터가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.validateUpdateCommunityRequest(
                            TEST_COMMUNITY_ID,
                            TEST_USER_ID,
                            listOf(TEST_COMMUNITY_CONTENT_IMAGE_DELETE_ID),
                            1,
                        )
                    }
                }
            }

            context("수정 요청자와 작성자가 다른 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        communityService.validateUpdateCommunityRequest(
                            TEST_COMMUNITY_ID,
                            TEST_OTHER_USER_ID,
                            listOf(TEST_COMMUNITY_CONTENT_IMAGE_DELETE_ID),
                            1,
                        )
                    }
                }
            }

            context("제한 범위 초과의 이미지가 주어지는 경우") {
                it("[IllegalArgumentException] 반환한다") {
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                    shouldThrow<IllegalArgumentException> {
                        communityService.validateUpdateCommunityRequest(
                            TEST_COMMUNITY_ID,
                            TEST_USER_ID,
                            listOf(TEST_COMMUNITY_CONTENT_IMAGE_DELETE_ID),
                            15,
                        )
                    }
                }
            }

            context("제한 범위 미만의 이미지가 주어지는 경우") {
                it("[IllegalArgumentException] 반환한다") {
                    every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                    shouldThrow<IllegalArgumentException> {
                        val deleteCommunityContentImageIds = (1..3).map { it.toLong() }
                        communityService.validateUpdateCommunityRequest(
                            TEST_COMMUNITY_ID,
                            TEST_USER_ID,
                            deleteCommunityContentImageIds,
                            1,
                        )
                    }
                }
            }
        }

        describe("updateCommunity") {
            context("유효한 데이터가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { travelJournalRepository.getByTravelJournalId(TEST_UPDATE_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    id = TEST_UPDATE_TRAVEL_JOURNAL_ID,
                )
                every { topicRepository.getByTopicId(TEST_UPDATE_TOPIC_ID) } returns createTopic(id = TEST_UPDATE_TOPIC_ID)
                every { communityRepository.deleteAllByIdInBatch(any<List<Long>>()) } just runs
                every { applicationEventPublisher.publishEvent(any<CommunityUpdateEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.updateCommunity(
                            TEST_COMMUNITY_ID,
                            TEST_USER_ID,
                            createCommunityUpdateRequest(),
                            createCommunityContentImageUpdatePairs(),
                        )
                    }
                }
            }

            context("수정할 커뮤니티 아이디가 없는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        communityService.updateCommunity(
                            TEST_COMMUNITY_ID,
                            TEST_USER_ID,
                            createCommunityUpdateRequest(),
                            createCommunityContentImageUpdatePairs(),
                        )
                    }
                }
            }

            context("수정할 여행 일지 작성자와 수정 요청자가 같지 않은 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { travelJournalRepository.getByTravelJournalId(TEST_UPDATE_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    user = createOtherUser(),
                )
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        communityService.updateCommunity(
                            TEST_COMMUNITY_ID,
                            TEST_USER_ID,
                            createCommunityUpdateRequest(),
                            createCommunityContentImageUpdatePairs(),
                        )
                    }
                }
            }
        }

        describe("deleteCommunity") {
            context("유효한 데이터가 주어지는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                every { communityLikeRepository.deleteAllByCommunityId(TEST_COMMUNITY_ID) } just runs
                every { communityCommentRepository.deleteAllByCommunityId(TEST_COMMUNITY_ID) } just runs
                every { communityRepository.delete(any<Community>()) } just runs
                every { applicationEventPublisher.publishEvent(any<CommunityDeleteEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        communityService.deleteCommunity(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("삭제할 커뮤니티 아이디가 없는 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } throws NoSuchElementException("$TEST_COMMUNITY_ID: 존재하지 않는 커뮤니티입니다.")
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        communityService.deleteCommunity(TEST_COMMUNITY_ID, TEST_USER_ID)
                    }
                }
            }

            context("커뮤니티 작성자와 삭제 요청자가 같지 않은 경우") {
                every { communityRepository.getByCommunityId(TEST_COMMUNITY_ID) } returns createCommunity()
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        communityService.deleteCommunity(TEST_COMMUNITY_ID, TEST_OTHER_USER_ID)
                    }
                }
            }
        }

        describe("deleteCommunityByUserId") {
            context("유효한 유저 ID가 들어오는 경우") {
                every { reportCommunityRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { communityLikeRepository.deleteCommunityLikes(TEST_COMMUNITY_ID) } just runs
                every { communityCommentRepository.deleteCommunityComments(TEST_USER_ID) } just runs
                every { communityRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { communityService.deleteCommunityByUserId(TEST_USER_ID) }
                }
            }
        }
    },
)
