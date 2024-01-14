package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournal
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalRepository
import kr.weit.odya.domain.representativetraveljournal.getSliceBy
import kr.weit.odya.domain.representativetraveljournal.getTargetSliceBy
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkRepository
import kr.weit.odya.domain.traveljournalbookmark.getTravelJournalIds
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.TEST_FILE_AUTHENTICATED_URL
import kr.weit.odya.support.TEST_NOT_EXIST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_OTHER_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_OTHER_USER
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_REP_TRAVEL_JOURNAL_SORT_TYPE
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_ID
import kr.weit.odya.support.TEST_USER
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createRepTravelJournal
import kr.weit.odya.support.createTravelJournal

class RepresentativeTravelJournalServiceTest : DescribeSpec(
    {
        val repTravelJournalRepository = mockk<RepresentativeTravelJournalRepository>()
        val userRepository = mockk<UserRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val fileService = mockk<FileService>()
        val followRepository = mockk<FollowRepository>()
        val travelJournalBookmarkRepository = mockk<TravelJournalBookmarkRepository>()
        val repTravelJournalService = RepresentativeTravelJournalService(
            userRepository = userRepository,
            travelJournalRepository = travelJournalRepository,
            repTravelJournalRepository = repTravelJournalRepository,
            followRepository = followRepository,
            fileService = fileService,
            travelJournalBookmarkRepository = travelJournalBookmarkRepository,
        )

        describe("createRepTravelJournal") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every {
                    repTravelJournalRepository.existsByUserAndTravelJournal(
                        TEST_USER,
                        TEST_TRAVEL_JOURNAL,
                    )
                } returns false
                every { repTravelJournalRepository.save(any<RepresentativeTravelJournal>()) } returns createRepTravelJournal()
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        repTravelJournalService.createRepTravelJournal(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID)
                    }
                }
            }

            context("유저가 존재하지 않는 경우") {
                every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환한다") {
                    shouldThrow<NoSuchElementException> {
                        repTravelJournalService.createRepTravelJournal(
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
                        repTravelJournalService.createRepTravelJournal(
                            TEST_USER_ID,
                            TEST_NOT_EXIST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }

            context("자신의 여행일지가 아닌 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_OTHER_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    user = createOtherUser(),
                )

                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        repTravelJournalService.createRepTravelJournal(
                            TEST_USER_ID,
                            TEST_OTHER_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }

            context("이미 대표 여행일지로 등록한 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every {
                    repTravelJournalRepository.existsByUserAndTravelJournal(
                        TEST_USER,
                        TEST_TRAVEL_JOURNAL,
                    )
                } returns true
                it("[ExistResourceException] 반환한다") {
                    shouldThrow<ExistResourceException> {
                        repTravelJournalService.createRepTravelJournal(
                            TEST_USER_ID,
                            TEST_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }
        }

        describe("getTargetRepTravelJournals") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_OTHER_USER_ID) } returns TEST_OTHER_USER
                every {
                    followRepository.existsByFollowerIdAndFollowingId(
                        TEST_USER_ID,
                        TEST_OTHER_USER_ID,
                    )
                } returns true
                every {
                    repTravelJournalRepository.getTargetSliceBy(
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_REP_TRAVEL_JOURNAL_SORT_TYPE,
                        TEST_OTHER_USER,
                        TEST_USER_ID,
                    )
                } returns listOf(createRepTravelJournal())
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                every { travelJournalBookmarkRepository.getTravelJournalIds(TEST_USER_ID) } returns listOf(
                    TEST_TRAVEL_JOURNAL_ID,
                )
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        repTravelJournalService.getTargetRepTravelJournals(
                            TEST_USER_ID,
                            TEST_OTHER_USER_ID,
                            TEST_DEFAULT_SIZE,
                            null,
                            TEST_REP_TRAVEL_JOURNAL_SORT_TYPE,
                        )
                    }
                }
            }
        }

        describe("getMyRepTravelJournals") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every {
                    repTravelJournalRepository.getSliceBy(
                        TEST_DEFAULT_SIZE,
                        null,
                        TEST_REP_TRAVEL_JOURNAL_SORT_TYPE,
                        TEST_USER,
                    )
                } returns listOf(createRepTravelJournal())
                every { fileService.getPreAuthenticatedObjectUrl(any<String>()) } returns TEST_FILE_AUTHENTICATED_URL
                every { travelJournalBookmarkRepository.getTravelJournalIds(TEST_USER_ID) } returns listOf(
                    TEST_TRAVEL_JOURNAL_ID,
                )
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        repTravelJournalService.getMyRepTravelJournals(
                            TEST_USER_ID,
                            TEST_DEFAULT_SIZE,
                            null,
                            TEST_REP_TRAVEL_JOURNAL_SORT_TYPE,
                        )
                    }
                }
            }
        }

        describe("deleteRepTravelJournal") {
            context("유효한 데이터가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_TRAVEL_JOURNAL_ID) } returns TEST_TRAVEL_JOURNAL
                every {
                    repTravelJournalRepository.deleteByUserAndTravelJournal(
                        TEST_USER,
                        TEST_TRAVEL_JOURNAL,
                    )
                } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny {
                        repTravelJournalService.deleteRepTravelJournal(TEST_USER_ID, TEST_TRAVEL_JOURNAL_ID)
                    }
                }
            }

            context("자신의 대표 여행일지가 아닌 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns TEST_USER
                every { travelJournalRepository.getByTravelJournalId(TEST_OTHER_TRAVEL_JOURNAL_ID) } returns createTravelJournal(
                    user = createOtherUser(),
                )
                it("[ForbiddenException] 반환한다") {
                    shouldThrow<ForbiddenException> {
                        repTravelJournalService.deleteRepTravelJournal(
                            TEST_USER_ID,
                            TEST_OTHER_TRAVEL_JOURNAL_ID,
                        )
                    }
                }
            }
        }
    },
)
