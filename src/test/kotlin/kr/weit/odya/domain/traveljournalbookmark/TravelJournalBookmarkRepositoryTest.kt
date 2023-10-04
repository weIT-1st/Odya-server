package kr.weit.odya.domain.traveljournalbookmark

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createOtherContentImage
import kr.weit.odya.support.createOtherTravelJournalBookmark
import kr.weit.odya.support.createOtherTravelJournalContentImage
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalBookmark
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TravelJournalBookmarkRepositoryTest(
    private val travelJournalBookmarkRepository: TravelJournalBookmarkRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var travelJournal: TravelJournal
        lateinit var otherTravelJournal: TravelJournal
        lateinit var travelJournalBookmark: TravelJournalBookmark
        lateinit var otherTravelJournalBookmark: TravelJournalBookmark
        beforeEach {
            user = userRepository.save(createUser())
            val travelJournals = listOf(
                createTravelJournal(
                    id = 1L,
                    user = user,
                    travelCompanions = emptyList(),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = user)),
                            ),
                        ),
                    ),
                ),
                createTravelJournal(
                    id = 2L,
                    user = user,
                    title = "otherTravelJournal",
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
                    travelCompanions = emptyList(),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createOtherTravelJournalContentImage(contentImage = createOtherContentImage(user = user)),
                            ),
                        ),
                    ),
                ),
            )
            val saveTravelJournals = travelJournalRepository.saveAll(travelJournals)
            travelJournal = saveTravelJournals[0]
            otherTravelJournal = saveTravelJournals[1]
            travelJournalBookmark = travelJournalBookmarkRepository.save(
                createTravelJournalBookmark(
                    travelJournal = travelJournal,
                    user = user,
                ),
            )
            otherTravelJournalBookmark = travelJournalBookmarkRepository.save(
                createOtherTravelJournalBookmark(
                    travelJournal = otherTravelJournal,
                    user = user,
                ),
            )
        }

        context("여행일지 즐겨찾기 여부 확인") {
            expect("유저, 여행일지가 일치하는 여행일지 즐겨찾기가 존재하는지 확인한다.") {
                travelJournalBookmarkRepository.existsByUserAndTravelJournal(user, travelJournal) shouldBe true
            }

            expect("유저 ID와 여행일지가 일치하는 여행일지 즐겨찾기가 존재하는지 확인한다.") {
                travelJournalBookmarkRepository.existsByUserIdAndTravelJournal(user.id, travelJournal) shouldBe true
            }
        }

        context("여행일지 즐겨찾기 조회") {
            expect("유저와 일치하는 여행일지 즐겨찾기 목록을 조회한다.") {
                travelJournalBookmarkRepository.findSliceBy(
                    TEST_DEFAULT_SIZE,
                    null,
                    TravelJournalBookmarkSortType.LATEST,
                    user,
                ).size shouldBe 2
            }
        }

        context("여행일지 즐겨찾기 삭제") {
            expect("유저, 여행일지가 일치하는 여행일지 즐겨찾기를 삭제한다.") {
                travelJournalBookmarkRepository.deleteByUserAndTravelJournal(user, travelJournal)
                travelJournalBookmarkRepository.existsByUserAndTravelJournal(user, travelJournal) shouldBe false
            }
        }
    },
)
