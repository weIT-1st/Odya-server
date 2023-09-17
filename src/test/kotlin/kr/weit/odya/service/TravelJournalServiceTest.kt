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
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelCompanionRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContentUpdateEvent
import kr.weit.odya.domain.traveljournal.TravelJournalDeleteEvent
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalSortType
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.traveljournal.getFriendTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getMyTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getRecommendTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getTravelJournalSliceBy
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_ANOTHER_USER_ID
import kr.weit.odya.support.TEST_CONTENT_IMAGES
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
import kr.weit.odya.support.TEST_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_OTHER_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PLACE_ID
import kr.weit.odya.support.TEST_TRAVEL_COMPANION_IDS
import kr.weit.odya.support.TEST_TRAVEL_COMPANION_USERS
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_IMAGE_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_NOT_EXIST_CONTENT_ID
import kr.weit.odya.support.TEST_USER
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createFollowerFcmTokenList
import kr.weit.odya.support.createImageMap
import kr.weit.odya.support.createImageNamePairs
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockImageFiles
import kr.weit.odya.support.createMockOtherImageFile
import kr.weit.odya.support.createOtherTravelJournalContentRequest
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createPlaceDetails
import kr.weit.odya.support.createPlaceDetailsMap
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalByTravelCompanionIdSize
import kr.weit.odya.support.createTravelJournalContentRequest
import kr.weit.odya.support.createTravelJournalContentUpdateRequest
import kr.weit.odya.support.createTravelJournalRequest
import kr.weit.odya.support.createTravelJournalRequestByContentSize
import kr.weit.odya.support.createTravelJournalUpdateRequest
import kr.weit.odya.support.createUpdateImageNamePairs
import kr.weit.odya.support.createUser
import org.mockito.ArgumentMatchers.any
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

class TravelJournalServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val fileService = mockk<FileService>()
        val reportTravelJournalRepository = mockk<ReportTravelJournalRepository>()
        val contentImageRepository = mockk<ContentImageRepository>()
        val travelCompanionRepository = mockk<TravelCompanionRepository>()
        val applicationEventPublisher = mockk<ApplicationEventPublisher>()
        val followRepository = mockk<FollowRepository>()
        val communityRepository = mockk<CommunityRepository>()
        val googleMapsClient = mockk<GoogleMapsClient>()
        val travelJournalService = TravelJournalService(
            userRepository,
            travelJournalRepository,
            followRepository,
            communityRepository,
            fileService,
            applicationEventPublisher,
            reportTravelJournalRepository,
            contentImageRepository,
            travelCompanionRepository,
            googleMapsClient,
        )

        describe("createTravelJournal") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val register = createUser()
                val imageNamePairs = createImageNamePairs()
                val placeDetailsMap = createPlaceDetailsMap()
                every { userRepository.getByUserId(TEST_USER_ID) } returns register
                every { userRepository.getByUserIds(TEST_TRAVEL_COMPANION_IDS) } returns TEST_TRAVEL_COMPANION_USERS
                every { travelJournalRepository.save(any<TravelJournal>()) } returns TEST_TRAVEL_JOURNAL
                every { followRepository.findFollowerFcmTokenByFollowingId(TEST_USER_ID) } returns createFollowerFcmTokenList()
                every { applicationEventPublisher.publishEvent(any<PushNotificationEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.createTravelJournal(
                            TEST_USER_ID,
                            travelJournalRequest,
                            imageNamePairs,
                            placeDetailsMap,
                        )
                    }
                }
            }

            context("유효한 데이터가 주어지는 경우(여행 친구 NULL)") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelCompanionIds = null,
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(),
                    ),
                )
                val imageNamePairs = createImageNamePairs()
                val placeDetailsMap = createPlaceDetailsMap()
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.save(any<TravelJournal>()) } returns TEST_TRAVEL_JOURNAL
                every { followRepository.findFollowerFcmTokenByFollowingId(TEST_USER_ID) } returns createFollowerFcmTokenList()
                every { applicationEventPublisher.publishEvent(any<PushNotificationEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.createTravelJournal(
                            TEST_USER_ID,
                            travelJournalRequest,
                            imageNamePairs,
                            placeDetailsMap,
                        )
                    }
                }
            }

            context("등록하려는 사용자가 없는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageNamePairs = createImageNamePairs()
                val placeDetailsMap = createPlaceDetailsMap()
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException("$TEST_USER_ID: 사용자가 존재하지 않습니다")
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalService.createTravelJournal(
                            TEST_USER_ID,
                            travelJournalRequest,
                            imageNamePairs,
                            placeDetailsMap,
                        )
                    }
                }
            }
        }

        describe("uploadTravelContentImages") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val contentImageNames =
                    travelJournalRequest.travelJournalContentRequests.flatMap { it.contentImageNames }
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { fileService.saveFile(any<MultipartFile>()) } returns TEST_GENERATED_FILE_NAME
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.uploadTravelContentImages(
                            contentImageNames,
                            imageMap,
                        )
                    }
                }
            }

            context("파일 업로드에 실패하는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val contentImageNames =
                    travelJournalRequest.travelJournalContentRequests.flatMap { it.contentImageNames }
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { fileService.saveFile(any<MultipartFile>()) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> {
                        travelJournalService.uploadTravelContentImages(
                            contentImageNames,
                            imageMap,
                        )
                    }
                }
            }
        }

        describe("validateTravelJournalRequest") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 이미지 이름의 개수와 실제 이미지의 개수가 다른 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(contentImageNames = listOf(TEST_IMAGE_FILE_WEBP)),
                    ),
                )
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 이미지 이름이 실제 이미지의 이름과 다른 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageMap =
                    createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME, fileName = "wrong_file_name.png")
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 친구가 최대 허용 인원을 넘는 경우") {
                val travelJournalRequest = createTravelJournalByTravelCompanionIdSize(11)
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 친구 ID가 등록되어 있지 않은 경우") {
                val notExistUserId = 0L
                val travelJournalRequest = createTravelJournalRequest(travelCompanionIds = listOf(notExistUserId))
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns false
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 시작일이 여행 종료일보다 이후일 경우") {
                val travelJournalRequest = createTravelJournalRequest(travelStartDate = LocalDate.parse("2021-12-12"))
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 기간이 허용 기간을 초과하는 경우") {
                val travelJournalRequest = createTravelJournalRequest(travelEndDate = LocalDate.parse("2021-01-31"))
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠 개수가 여행 기간보다 큰 경우") {
                val travelJournalRequest = createTravelJournalRequestByContentSize(3)
                // 아래와 같이 사진 개수도 같이 맞춰주지 않으면 여행기간보다 콘텐츠 개수가 많아서 IllegalArgumentException이 발생하는게 아니라 사진 개수가 안맞아서 발생한다.
                val imageMap = mapOf(
                    TEST_IMAGE_FILE_WEBP to createMockImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME),
                    TEST_OTHER_IMAGE_FILE_WEBP to createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME),
                    TEST_GENERATED_FILE_NAME to createMockOtherImageFile(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME),
                )
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 여행 일자가 여행 기간을 벗어나는 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(travelDate = LocalDate.parse("2022-01-01")),
                        // 아래와 같이 여행 콘텐츠 개수를 맞춰주지 않으면 여행 기간을 벗어나서 IllegalArgumentException이 발생하는게 아니라 사진 개수로 인해 발생한다.
                        // 다른 테스트 들도 전부 같은 이유로 createOtherTravelJournalContentRequest()가 있어야 한다
                        createOtherTravelJournalContentRequest(),
                    ),
                )
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 위도와 경도 개수가 다른 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(
                            latitudes = listOf(37.123456),
                            longitudes = listOf(127.123456, 127.123456),
                        ),
                        createOtherTravelJournalContentRequest(),
                    ),
                )
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 위도와 경도중 하나만 보냈을 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(
                            latitudes = null,
                            longitudes = listOf(127.123456, 127.123456),
                        ),
                        createOtherTravelJournalContentRequest(),
                    ),
                )
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 위도와 경도를 둘다 안 보냈을 경우") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(
                            latitudes = null,
                            longitudes = null,
                        ),
                        createOtherTravelJournalContentRequest(),
                    ),
                )
                val imageMap = createImageMap(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                every { userRepository.existsAllByUserIds(any<Collection<Long>>(), any<Int>()) } returns true
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.validateTravelJournalRequest(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }
        }

        describe("getImageMap") {
            context("유효한 데이터가 주어지는 경우") {
                val mockFiles = createMockImageFiles(mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getImageMap(
                            mockFiles,
                        )
                    }
                }
            }

            context("파일 OriginName이 없는 경우") {
                val mockFiles = listOf(
                    createMockImageFile(
                        mockFileName = TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME,
                        originalFileName = null,
                    ),
                )
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.getImageMap(
                            mockFiles,
                        )
                    }
                }
            }
        }

        describe("getTravelJournal") {
            context("모두 공개 여행 일지가 주어지는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getTravelJournal(TEST_TRAVEL_JOURNAL.id, TEST_USER_ID)
                    }
                }
            }

            context("친구에게만 공개 여행 일지가 주어지고, 요청자가 친구인 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
                )
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                    )
                } returns true
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getTravelJournal(TEST_TRAVEL_JOURNAL.id, TEST_OTHER_USER_ID)
                    }
                }
            }

            context("친구에게만 공개 여행 일지가 주어지고, 요청자가 친구가 아닌 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
                )
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                    )
                } throws ForbiddenException(
                    "친구가 아닌 사용자는 친구에게만 공개하는 여행 일지를 조회할 수 없습니다.",
                )
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        travelJournalService.getTravelJournal(TEST_TRAVEL_JOURNAL.id, TEST_OTHER_USER_ID)
                    }
                }
            }

            context("비공개 여행 일지가 주어지고, 요청자가 작성자인 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    visibility = TravelJournalVisibility.PRIVATE,
                )
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getTravelJournal(TEST_TRAVEL_JOURNAL.id, TEST_USER_ID)
                    }
                }
            }

            context("비공개 여행 일지가 주어지고, 요청자와 작성자가 일치하지 않는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    visibility = TravelJournalVisibility.PRIVATE,
                )
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        travelJournalService.getTravelJournal(TEST_TRAVEL_JOURNAL.id, TEST_OTHER_USER_ID)
                    }
                }
            }
        }

        describe("getTravelJournals") {
            context("유효한 데이터가 주어지는 경우") {
                every {
                    travelJournalRepository.getTravelJournalSliceBy(
                        TEST_USER_ID,
                        10,
                        null,
                        TravelJournalSortType.LATEST,
                    )
                } returns listOf(TEST_TRAVEL_JOURNAL)
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getTravelJournals(
                            TEST_USER_ID,
                            10,
                            null,
                            TravelJournalSortType.LATEST,
                        )
                    }
                }
            }
        }

        describe("getMyTravelJournals") {
            context("유효한 데이터가 주어지는 경우") {
                every {
                    travelJournalRepository.getMyTravelJournalSliceBy(
                        TEST_USER_ID,
                        10,
                        null,
                        TravelJournalSortType.LATEST,
                    )
                } returns listOf(TEST_TRAVEL_JOURNAL)
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getMyTravelJournals(
                            TEST_USER_ID,
                            10,
                            null,
                            TravelJournalSortType.LATEST,
                        )
                    }
                }
            }
        }

        describe("getFriendTravelJournals") {
            context("유효한 데이터가 주어지는 경우") {
                every {
                    travelJournalRepository.getFriendTravelJournalSliceBy(
                        TEST_OTHER_USER_ID,
                        10,
                        null,
                        TravelJournalSortType.LATEST,
                    )
                } returns listOf(TEST_TRAVEL_JOURNAL)
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getFriendTravelJournals(
                            TEST_OTHER_USER_ID,
                            10,
                            null,
                            TravelJournalSortType.LATEST,
                        )
                    }
                }
            }
        }

        describe("getRecommendTravelJournals") {
            context("유효한 데이터가 주어지는 경우") {
                val otherUser = createOtherUser()
                every { userRepository.getByUserId(TEST_OTHER_USER_ID) } returns otherUser
                every {
                    travelJournalRepository.getRecommendTravelJournalSliceBy(
                        otherUser,
                        10,
                        null,
                        TravelJournalSortType.LATEST,
                    )
                } returns listOf(TEST_TRAVEL_JOURNAL)
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getRecommendTravelJournals(
                            TEST_OTHER_USER_ID,
                            10,
                            null,
                            TravelJournalSortType.LATEST,
                        )
                    }
                }
            }
        }

        describe("updateTravelJournal") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalUpdateRequest = createTravelJournalUpdateRequest()
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                every { travelCompanionRepository.deleteAllByIdInBatch(any<List<Long>>()) } just runs
                every { userRepository.getByUserIds(any<List<Long>>()) } returns listOf(createCustomUser())
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }

            context("여행 시작일이 여행 종료일보다 이후인 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelStartDate = LocalDate.of(2022, 1, 3))
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }

            context("작성자와 수정 요청자가 일치하지 않는 경우") {
                val travelJournalUpdateRequest = createTravelJournalUpdateRequest()
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_ANOTHER_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }

            context("여행 일지의 여행 기간이 제한 기간을 초과하는 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelEndDate = LocalDate.of(2021, 1, 16))
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }

            context("여행 기간이 콘텐츠 개수보다 작은 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelEndDate = LocalDate.of(2021, 1, 1))
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 일자가 여행 기간을 벗어나는 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(travelEndDate = LocalDate.of(2022, 1, 1))
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }

            context("여행 친구가 제한 인원을 초과하는 경우") {
                val travelJournalUpdateRequest =
                    createTravelJournalUpdateRequest(
                        travelCompanionIds = (0 until 10).map { it.toLong() }.toList(),
                    )
                val travelJournal = createTravelJournal()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns travelJournal
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.updateTravelJournal(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_USER_ID,
                            travelJournalUpdateRequest,
                        )
                    }
                }
            }
        }

        describe("updateTravelJournalContent") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { applicationEventPublisher.publishEvent(any<TravelJournalContentUpdateEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.updateTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            travelJournalContentUpdateRequest,
                            createUpdateImageNamePairs(),
                            createPlaceDetailsMap(),
                        )
                    }
                }
            }

            context("ID에 맞는 여행 일지 콘텐츠가 없는 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalService.updateTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_NOT_EXIST_CONTENT_ID,
                            TEST_USER_ID,
                            travelJournalContentUpdateRequest,
                            createUpdateImageNamePairs(),
                            createPlaceDetailsMap(),
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠 이미지 수정으로 남아 있는 개수가 1보다 작은 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    updateContentImageNames = emptyList(),
                    deleteContentImageIds = listOf(
                        TEST_TRAVEL_JOURNAL_CONTENT_IMAGE_ID,
                    ),
                )
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.updateTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            travelJournalContentUpdateRequest,
                            emptyList(),
                            createPlaceDetailsMap(),
                        )
                    }
                }
            }
        }

        describe("validateTravelJournalContentUpdateRequest") {
            context("유효한 데이터가 주어지는 경우") {
                val imageMap = createImageMap(
                    mockFileName = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                    fileName = "updateTestImage.webp",
                    otherFileName = "updateTestImage2.webp",
                )
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.validateTravelJournalContentUpdateRequest(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            imageMap,
                            travelJournalContentUpdateRequest,
                        )
                    }
                }
            }

            context("작성자와 수정 요청자가 일치하지 않는 경우") {
                val imageMap = createImageMap(
                    mockFileName = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                    fileName = "updateTestImage.webp",
                    otherFileName = "updateTestImage2.webp",
                )
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest()
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        travelJournalService.validateTravelJournalContentUpdateRequest(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_OTHER_USER_ID,
                            imageMap,
                            travelJournalContentUpdateRequest,
                        )
                    }
                }
            }

            context("이미지의 수가 제한 개수를 초과하는 경우") {
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    updateContentImageNames = (0 until 15).map { "updateTestImage$it.webp" }.toList(),
                )
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalContentUpdateRequest(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            any<Map<String, MultipartFile>>(),
                            travelJournalContentUpdateRequest,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠 이미지의 이름과 실제 이미지의 이름이 다른 경우") {
                val imageMap = createImageMap(
                    mockFileName = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                    fileName = "updateTestImage.webp",
                    otherFileName = "updateTestImage2.webp",
                )
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    updateContentImageNames = listOf("otherImage.webp", "otherImage2.webp"),
                )
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalContentUpdateRequest(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            imageMap,
                            travelJournalContentUpdateRequest,
                        )
                    }
                }
            }

            context("여행 일지 콘텐츠의 여행 일자가 여행 기간을 벗어나는 경우") {
                val imageMap = createImageMap(
                    mockFileName = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                    fileName = "updateTestImage.webp",
                    otherFileName = "updateTestImage2.webp",
                )
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    travelDate = LocalDate.of(2022, 10, 10),
                )
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalContentUpdateRequest(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            imageMap,
                            travelJournalContentUpdateRequest,
                        )
                    }
                }
            }

            context("여행 일지 위도와 경도의 개수가 다른 경우") {
                val imageMap = createImageMap(
                    mockFileName = TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME,
                    fileName = "updateTestImage.webp",
                    otherFileName = "updateTestImage2.webp",
                )
                val travelJournalContentUpdateRequest = createTravelJournalContentUpdateRequest(
                    latitudes = listOf(123.123),
                )
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.validateTravelJournalContentUpdateRequest(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                            imageMap,
                            travelJournalContentUpdateRequest,
                        )
                    }
                }
            }
        }

        describe("deleteTravelJournalContent") {
            context("유요한 데이터가 주어지는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                every { applicationEventPublisher.publishEvent(any<TravelJournalDeleteEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.deleteTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                        )
                    }
                }
            }

            context("요청한 사용자와 작성자가 일치하지 않는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        travelJournalService.deleteTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_OTHER_USER_ID,
                        )
                    }
                }
            }

            context("여행일지 콘텐츠가 존재하지 않는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalService.deleteTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_NOT_EXIST_CONTENT_ID,
                            TEST_USER_ID,
                        )
                    }
                }
            }
        }

        describe("deleteTravelJournal") {
            context("유요한 데이터가 주어지는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                every { communityRepository.updateTravelJournalIdToNull(TEST_TRAVEL_JOURNAL_ID) } just runs
                every { applicationEventPublisher.publishEvent(any<TravelJournalDeleteEvent>()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.deleteTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_USER_ID,
                        )
                    }
                }
            }

            context("요청한 사용자와 작성자가 일치하지 않는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        travelJournalService.deleteTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_CONTENT_ID,
                            TEST_OTHER_USER_ID,
                        )
                    }
                }
            }

            context("여행일지 콘텐츠가 존재하지 않는 경우") {
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns createTravelJournal()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalService.deleteTravelJournalContent(
                            TEST_TRAVEL_JOURNAL_ID,
                            TEST_TRAVEL_JOURNAL_NOT_EXIST_CONTENT_ID,
                            TEST_USER_ID,
                        )
                    }
                }
            }
        }

        describe("getPlaceDetailsMap") {
            context("유효한 데이터가 주어지는 경우") {
                val placeIds = setOf(TEST_PLACE_ID)
                every { googleMapsClient.findPlaceDetailsByPlaceId(TEST_PLACE_ID) } returns createPlaceDetails()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getPlaceDetailsMap(
                            placeIds,
                        )
                    }
                }
            }

            context("존재 하지 않는 placeId를 넣은 경우") {
                val placeIds = setOf(TEST_PLACE_ID)
                every { googleMapsClient.findPlaceDetailsByPlaceId(TEST_PLACE_ID) } throws InvalidRequestException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[InvalidRequestException] 반환한다") {
                    shouldThrow<InvalidRequestException> {
                        travelJournalService.getPlaceDetailsMap(
                            placeIds,
                        )
                    }
                }
            }
        }

        describe("deleteTravelJournalByUserId") {
            context("유효한 UserId가 주어지는 경우") {
                it("정상적으로 종료한다") {
                    every { contentImageRepository.findAllByUserId(TEST_USER_ID) } returns TEST_CONTENT_IMAGES
                    every { fileService.deleteFile(any()) } just runs
                    every { contentImageRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                    every { reportTravelJournalRepository.deleteAllByCommonReportInformationUserId(TEST_USER_ID) } just runs
                    every { travelCompanionRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                    every { travelJournalRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                    shouldNotThrowAny {
                        travelJournalService.deleteTravelJournalByUserId(TEST_USER_ID)
                    }
                }
            }
        }
    },
)
