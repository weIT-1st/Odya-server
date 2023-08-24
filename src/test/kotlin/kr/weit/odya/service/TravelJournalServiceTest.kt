package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.support.SOMETHING_ERROR_MESSAGE
import kr.weit.odya.support.TEST_CONTENT_IMAGES
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
import kr.weit.odya.support.TEST_IMAGE_FILE_WEBP
import kr.weit.odya.support.TEST_TRAVEL_COMPANION_IDS
import kr.weit.odya.support.TEST_TRAVEL_COMPANION_USERS
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL
import kr.weit.odya.support.TEST_USER
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createImageMap
import kr.weit.odya.support.createImageNamePairs
import kr.weit.odya.support.createMockImageFile
import kr.weit.odya.support.createMockImageFiles
import kr.weit.odya.support.createTravelJournalByTravelCompanionIdSize
import kr.weit.odya.support.createTravelJournalContentRequest
import kr.weit.odya.support.createTravelJournalRequest
import kr.weit.odya.support.createTravelJournalRequestByContentSize
import kr.weit.odya.support.createUser
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

class TravelJournalServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val contentImageRepository = mockk<ContentImageRepository>()
        val fileService = mockk<FileService>()
        val travelJournalService =
            TravelJournalService(userRepository, travelJournalRepository, contentImageRepository, fileService)

        describe("createTravelJournal") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val register = createUser()
                val imageNamePairs = createImageNamePairs()
                every { userRepository.getByUserId(TEST_USER_ID) } returns register
                every { userRepository.getByUserIds(TEST_TRAVEL_COMPANION_IDS) } returns TEST_TRAVEL_COMPANION_USERS
                every { contentImageRepository.saveAll(any<Iterable<ContentImage>>()) } returns TEST_CONTENT_IMAGES
                every { travelJournalRepository.save(any<TravelJournal>()) } returns TEST_TRAVEL_JOURNAL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.createTravelJournal(
                            TEST_USER_ID,
                            travelJournalRequest,
                            imageNamePairs,
                        )
                    }
                }
            }

            context("유효한 데이터가 주어지는 경우(여행 친구 및 이미지 NULL)") {
                val travelJournalRequest = createTravelJournalRequest(
                    travelCompanionIds = null,
                    travelJournalContentRequests = listOf(
                        createTravelJournalContentRequest(contentImageNames = null),
                    ),
                )
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.save(any<TravelJournal>()) } returns TEST_TRAVEL_JOURNAL
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.createTravelJournal(
                            TEST_USER_ID,
                            travelJournalRequest,
                            emptyList(),
                        )
                    }
                }
            }

            context("등록하려는 사용자가 없는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageNamePairs = createImageNamePairs()
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException("$TEST_USER_ID: 사용자가 존재하지 않습니다")
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalService.createTravelJournal(
                            TEST_USER_ID,
                            travelJournalRequest,
                            imageNamePairs,
                        )
                    }
                }
            }
        }

        describe("uploadTravelContentImages") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageMap = createImageMap()
                every { fileService.saveFile(any<MultipartFile>()) } returns TEST_GENERATED_FILE_NAME
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.uploadTravelContentImages(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }

            context("파일 업로드에 실패하는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageMap = createImageMap()
                every { fileService.saveFile(any<MultipartFile>()) } throws ObjectStorageException(
                    SOMETHING_ERROR_MESSAGE,
                )
                it("[ObjectStorageException] 반환한다") {
                    shouldThrow<ObjectStorageException> {
                        travelJournalService.uploadTravelContentImages(
                            travelJournalRequest,
                            imageMap,
                        )
                    }
                }
            }
        }

        describe("validateTravelJournalRequest") {
            context("유효한 데이터가 주어지는 경우") {
                val travelJournalRequest = createTravelJournalRequest()
                val imageMap = createImageMap()
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
                val imageMap = createImageMap()
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
                val imageMap = createImageMap(fileName = "wrong_file_name.png")
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
                val imageMap = createImageMap()
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
                val imageMap = createImageMap()
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
                val imageMap = createImageMap()
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
                val imageMap = createImageMap()
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
                val imageMap = createImageMap()
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
                    ),
                )
                val imageMap = createImageMap()
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
                    ),
                )
                val imageMap = createImageMap()
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
        }

        describe("getImageMap") {
            context("유효한 데이터가 주어지는 경우") {
                val mockFiles = createMockImageFiles()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalService.getImageMap(
                            mockFiles,
                        )
                    }
                }
            }

            context("파일 OriginName이 없는 경우") {
                val mockFiles = listOf(createMockImageFile(originalFileName = null))
                it("[IllegalArgumentException] 반환한다") {
                    shouldThrow<IllegalArgumentException> {
                        travelJournalService.getImageMap(
                            mockFiles,
                        )
                    }
                }
            }
        }
    },
)
