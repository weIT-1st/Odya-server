package kr.weit.odya.domain.traveljournal

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_TRAVEL_JOURNAL_TITLE
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TravelJournalRepositoryTest(
    private val userRepository: UserRepository,
    private val followRepository: FollowRepository,
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var travelJournal: TravelJournal
        lateinit var otherTravelJournal: TravelJournal
        lateinit var friendTravelJournal: TravelJournal
        beforeEach {
            user = userRepository.save(createUser())
            otherUser = userRepository.save(createOtherUser())
            followRepository.save(Follow(user, otherUser))
            val travelJournals = listOf(
                createTravelJournal(
                    id = 1L,
                    user = user,
                    travelCompanions = listOf(createTravelCompanionById(user = otherUser)),
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
                    travelCompanions = listOf(createTravelCompanionById(user = otherUser)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = user)),
                            ),
                        ),
                    ),
                ),
                createTravelJournal(
                    id = 3L,
                    user = otherUser,
                    title = "friendTravelJournal",
                    travelCompanions = listOf(createTravelCompanionById(user = user)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = otherUser)),
                            ),
                        ),
                    ),
                ),
            )
            val saveTravelJournals = travelJournalRepository.saveAll(travelJournals)
            travelJournal = saveTravelJournals[0]
            otherTravelJournal = saveTravelJournals[1]
            friendTravelJournal = saveTravelJournals[2]
        }

        context("여행 일지 조회") {
            expect("여행 일지 ID와 일치하는 여행 일지를 조회한다.") {
                val result = travelJournalRepository.getByTravelJournalId(travelJournal.id)
                result.title shouldBe TEST_TRAVEL_JOURNAL_TITLE
            }
        }

        context("여행 일지 사용자 Id로 조회") {
            expect("유저 ID와 일치하는 여행기록을 조회한다.") {
                val result = travelJournalRepository.getByUserId(user.id)
                result.size shouldBe 2
            }
        }

        context("여행 일지 목록 조회") {
            expect("나의 여행 일지 목록을 조회한다.") {
                val result = travelJournalRepository.getMyTravelJournalSliceBy(
                    userId = user.id,
                    size = 10,
                    lastId = null,
                    sortType = TravelJournalSortType.LATEST,
                )
                result.size shouldBe 2
            }

            expect("내 친구의 여행 일지 목록을 조회한다.") {
                val result = travelJournalRepository.getFriendTravelJournalSliceBy(
                    userId = user.id,
                    size = 10,
                    lastId = null,
                    sortType = TravelJournalSortType.LATEST,
                )
                result.size shouldBe 1
                result[0] shouldBe friendTravelJournal
            }

            expect("추천 여행 일지 목록을 조회한다.") {
                val result = travelJournalRepository.getRecommendTravelJournalSliceBy(
                    user = user,
                    size = 10,
                    lastId = null,
                    sortType = TravelJournalSortType.LATEST,
                )
                result.size shouldBe 1
                result[0] shouldBe friendTravelJournal
            }
        }
    },
)
