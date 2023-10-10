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
            followRepository.save(Follow(otherUser, user))
            val travelJournals = listOf(
                createTravelJournal(
                    id = 1L,
                    user = user,
                    travelCompanions = listOf(createTravelCompanionById(user = otherUser)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = user)),
                                createTravelJournalContentImage(contentImage = createContentImage(name = "test1.webp", originName = "test1.webp", user = user)),
                            ),
                        ),
                    ),
                ),
                createTravelJournal(
                    id = 2L,
                    user = user,
                    title = "otherTravelJournal",
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
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
            expect("여행 일지 목록을 조회한다.") {
                val result = travelJournalRepository.getTravelJournalSliceBy(
                    userId = otherUser.id,
                    size = 10,
                    lastId = null,
                    sortType = TravelJournalSortType.LATEST,
                )
                result.size shouldBe 3
            }

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

            expect("유저가 태그된 여행일지 목록을 조회한다.") {
                val result = travelJournalRepository.getTaggedTravelJournalSliceBy(
                    user = user,
                    size = 10,
                    lastId = null,
                )
                result.size shouldBe 1
                result[0] shouldBe friendTravelJournal
            }
        }

        context("ContentImage name 조회") {
            expect("여행 일지 ID와 일치하는 ContentImage name를 조회한다.") {
                val result = travelJournalRepository.getByContentImageNames(travelJournal.id)
                result shouldBe listOf("generated_file.webp", "test1.webp")
            }
        }

        context("TravelCompanion Id 조회") {
            expect("UserId와 TravelJournalId가 일치하는 TravelCompanion Id를 조회(존재)") {
                travelJournalRepository.findByUserIdAndTravelJournalId(otherUser, travelJournal.id) shouldBe travelJournal.travelCompanions[0].id
            }

            expect("UserId와 TravelJournalId가 일치하는 TravelCompanion Id를 조회(존재하지 않음)") {
                travelJournalRepository.findByUserIdAndTravelJournalId(user, travelJournal.id) shouldBe null
            }
        }

        context("여행 일지 삭제") {
            expect("여행 일지 ID와 일치하는 여행 일지를 삭제한다.") {
                travelJournalRepository.deleteById(travelJournal.id)
                travelJournalRepository.existsById(travelJournal.id) shouldBe false
            }

            expect("USER ID와 일치하는 여행 일지 모두 삭제한다.") {
                travelJournalRepository.deleteAllByUserId(user.id)
                travelJournalRepository.count() shouldBe 1
            }
        }
    },
)
