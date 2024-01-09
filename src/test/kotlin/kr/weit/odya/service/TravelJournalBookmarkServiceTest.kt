package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmark
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkRepository
import kr.weit.odya.domain.traveljournalbookmark.getSliceBy
import kr.weit.odya.domain.traveljournalbookmark.getSliceByOther
import kr.weit.odya.domain.traveljournalbookmark.getTravelJournalIds
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_NOT_EXIST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_USER
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createTravelJournalBookmark

class TravelJournalBookmarkServiceTest : DescribeSpec(
    {
        val travelJournalBookmarkRepository = mockk<TravelJournalBookmarkRepository>()
        val userRepository = mockk<UserRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val fileService = mockk<FileService>()
        val followRepository = mockk<FollowRepository>()
        val travelJournalBookmarkService =
            TravelJournalBookmarkService(
                travelJournalBookmarkRepository = travelJournalBookmarkRepository,
                userRepository = userRepository,
                travelJournalRepository = travelJournalRepository,
                fileService = fileService,
                followRepository,
            )

        describe("createTravelJournalBookmark") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every {
                    travelJournalBookmarkRepository.existsByUserAndTravelJournal(
                        TEST_USER,
                        TEST_TRAVEL_JOURNAL,
                    )
                } returns false
                every { travelJournalBookmarkRepository.save(any<TravelJournalBookmark>()) } returns createTravelJournalBookmark()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalBookmarkService.createTravelJournalBookmark(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID)
                    }
                }
            }

            context("유저가 존재하지 않는 경우") {
                every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalBookmarkService.createTravelJournalBookmark(
                            TEST_NOT_EXIST_USER_ID,
                            TEST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }

            context("여행일지가 존재하지 않는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_NOT_EXIST_TRAVEL_JOURNAL_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalBookmarkService.createTravelJournalBookmark(
                            TEST_USER_ID,
                            TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }

            context("이미 여행일지 즐겨찾기를 한 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every {
                    travelJournalBookmarkRepository.existsByUserAndTravelJournal(
                        TEST_USER,
                        TEST_TRAVEL_JOURNAL,
                    )
                } returns true
                it("[ExistResourceException] 반환한다") {
                    shouldThrow<ExistResourceException> {
                        travelJournalBookmarkService.createTravelJournalBookmark(
                            TEST_USER_ID,
                            TEST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }
        }

        describe("getMyTravelJournalBookmarks") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every {
                    travelJournalBookmarkRepository.getSliceBy(
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                        TEST_USER,
                    )
                } returns listOf(createTravelJournalBookmark())
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_USER_ID) } returns listOf(TEST_OTHER_USER_ID)
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalBookmarkService.getMyTravelJournalBookmarks(
                            TEST_USER_ID,
                            TEST_DEFAULT_SIZE,
                            null,
                            TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                        )
                    }
                }

                context("유저가 존재하지 않는 경우") {
                    every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                    it("[NoSuchElementException] 반환한다") {
                        shouldThrow<NoSuchElementException> {
                            travelJournalBookmarkService.getMyTravelJournalBookmarks(
                                TEST_NOT_EXIST_USER_ID,
                                TEST_DEFAULT_SIZE,
                                null,
                                TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                            )
                        }
                    }
                }
            }
        }

        describe("getOtherTravelJournalBookmarks") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every {
                    travelJournalBookmarkRepository.getSliceByOther(
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                        TEST_USER,
                        TEST_OTHER_USER_ID,
                    )
                } returns listOf(createTravelJournalBookmark())
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                every { followRepository.findFollowingIdsByFollowerId(TEST_OTHER_USER_ID) } returns listOf(TEST_USER_ID)
                every { travelJournalBookmarkRepository.getTravelJournalIds(TEST_OTHER_USER_ID) } returns listOf(TEST_TRAVEL_JOURNAL_ID)
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalBookmarkService.getOtherTravelJournalBookmarks(
                            TEST_OTHER_USER_ID,
                            TEST_USER_ID,
                            TEST_DEFAULT_SIZE,
                            null,
                            TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                        )
                    }
                }

                context("유저가 존재하지 않는 경우") {
                    every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                    it("[NoSuchElementException] 반환한다") {
                        shouldThrow<NoSuchElementException> {
                            travelJournalBookmarkService.getOtherTravelJournalBookmarks(
                                TEST_OTHER_USER_ID,
                                TEST_NOT_EXIST_USER_ID,
                                TEST_DEFAULT_SIZE,
                                null,
                                TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE,
                            )
                        }
                    }
                }
            }
        }

        describe("deleteTravelJournalBookmark") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every {
                    travelJournalBookmarkRepository.deleteByUserAndTravelJournal(
                        TEST_USER,
                        TEST_TRAVEL_JOURNAL,
                    )
                } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        travelJournalBookmarkService.deleteTravelJournalBookmark(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID)
                    }
                }
            }

            context("유저가 존재하지 않는 경우") {
                every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalBookmarkService.deleteTravelJournalBookmark(
                            TEST_NOT_EXIST_USER_ID,
                            TEST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }

            context("여행일지가 존재하지 않는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_NOT_EXIST_TRAVEL_JOURNAL_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        travelJournalBookmarkService.deleteTravelJournalBookmark(
                            TEST_USER_ID,
                            TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }
        }
    },
)
