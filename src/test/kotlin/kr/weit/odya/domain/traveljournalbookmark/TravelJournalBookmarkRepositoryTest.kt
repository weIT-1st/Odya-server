package kr.weit.odya.domain.traveljournalbookmark

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createOtherContentImage
import kr.weit.odya.support.createOtherTravelJournalBookmark
import kr.weit.odya.support.createOtherTravelJournalContentImage
import kr.weit.odya.support.createOtherUser
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
    private val followRepository: FollowRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var loginUser: User
        lateinit var otherUser: User
        lateinit var travelJournal: TravelJournal
        lateinit var otherTravelJournal: TravelJournal
        lateinit var otherTravelJournal2: TravelJournal
        lateinit var travelJournalBookmark: TravelJournalBookmark
        lateinit var otherTravelJournalBookmark: TravelJournalBookmark
        lateinit var otherTravelJournalBookmark2: TravelJournalBookmark
        beforeEach {
            user = userRepository.save(createUser())
            loginUser = userRepository.save(createOtherUser())
            otherUser = userRepository.save(createCustomUser("otherUser", "otherUser"))
            followRepository.save(createFollow(user, loginUser))
            followRepository.save(createFollow(loginUser, user))
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

                createTravelJournal(
                    id = 3L,
                    user = otherUser,
                    title = "otherTravelJournal2",
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
                    travelCompanions = emptyList(),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createOtherTravelJournalContentImage(contentImage = createOtherContentImage(user = otherUser)),
                            ),
                        ),
                    ),
                ),
            )

            val saveTravelJournals = travelJournalRepository.saveAll(travelJournals)
            travelJournal = saveTravelJournals[0]
            otherTravelJournal = saveTravelJournals[1]
            otherTravelJournal2 = saveTravelJournals[2]
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
            otherTravelJournalBookmark2 = travelJournalBookmarkRepository.save(
                createOtherTravelJournalBookmark(
                    travelJournal = otherTravelJournal2,
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
                ).size shouldBe 3
            }

            expect("해당 유저와 일치하는 여행일지 즐겨찾기 목록을 조회한다.") {
                travelJournalBookmarkRepository.findSliceByOther(
                    TEST_DEFAULT_SIZE,
                    null,
                    TravelJournalBookmarkSortType.LATEST,
                    user,
                    loginUser.id,
                ).size shouldBe 2
            }
        }

        context("여행일지 즐겨찾기 삭제") {
            expect("유저, 여행일지가 일치하는 여행일지 즐겨찾기를 삭제한다.") {
                travelJournalBookmarkRepository.deleteByUserAndTravelJournal(user, travelJournal)
                travelJournalBookmarkRepository.existsByUserAndTravelJournal(user, travelJournal) shouldBe false
            }

            expect("여행일지 아이디와 일치하는 모든 여행일지 즐겨찾기를 삭제한다.") {
                travelJournalBookmarkRepository.deleteAllByTravelJournalId(travelJournal.id)
                travelJournalBookmarkRepository.findAll().size shouldBe 2
            }

            expect("유저 아이디와 일치하는 모든 여행일지 즐겨찾기를 삭제한다.") {
                travelJournalBookmarkRepository.deleteAllByUserId(user.id)
                travelJournalBookmarkRepository.findAll().size shouldBe 0
            }
        }
    },
)
