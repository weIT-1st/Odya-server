package kr.weit.odya.domain.representativetraveljournal

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
import kr.weit.odya.support.createFollow
import kr.weit.odya.support.createOtherContentImage
import kr.weit.odya.support.createOtherTravelJournalContentImage
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createRepTravelJournal
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class RepresentativeTravelJournalRepositoryTest(
    private val repTravelJournalRepository: RepresentativeTravelJournalRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val followRepository: FollowRepository,
) : ExpectSpec(
    {
        lateinit var loginUser: User
        lateinit var targetUser: User
        lateinit var travelJournal: TravelJournal
        lateinit var targetTravelJournal1: TravelJournal
        lateinit var targetTravelJournal2: TravelJournal
        beforeEach {
            loginUser = userRepository.save(createUser())
            targetUser = userRepository.save(createOtherUser())
            followRepository.save(createFollow(loginUser, targetUser))
            followRepository.save(createFollow(targetUser, loginUser))
            val travelJournals = listOf(
                createTravelJournal(
                    id = 1L,
                    user = loginUser,
                    travelCompanions = emptyList(),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = loginUser)),
                            ),
                        ),
                    ),
                ),
                createTravelJournal(
                    id = 2L,
                    user = targetUser,
                    title = "targetTravelJournal1",
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
                    travelCompanions = emptyList(),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createOtherTravelJournalContentImage(contentImage = createOtherContentImage(user = targetUser)),
                            ),
                        ),
                    ),
                ),

                createTravelJournal(
                    id = 3L,
                    user = targetUser,
                    title = "targetTravelJournal2",
                    visibility = TravelJournalVisibility.FRIEND_ONLY,
                    travelCompanions = emptyList(),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createOtherTravelJournalContentImage(contentImage = createOtherContentImage(user = targetUser)),
                            ),
                        ),
                    ),
                ),
            )

            val saveTravelJournals = travelJournalRepository.saveAll(travelJournals)
            travelJournal = saveTravelJournals[0]
            targetTravelJournal1 = saveTravelJournals[1]
            targetTravelJournal2 = saveTravelJournals[2]
            repTravelJournalRepository.save(
                createRepTravelJournal(
                    travelJournal = travelJournal,
                    user = loginUser,
                ),
            )
            repTravelJournalRepository.save(
                createRepTravelJournal(
                    travelJournal = targetTravelJournal1,
                    user = targetUser,
                ),
            )
            repTravelJournalRepository.save(
                createRepTravelJournal(
                    travelJournal = targetTravelJournal2,
                    user = targetUser,
                ),
            )
        }

        context("대표 여행일지 조회") {
            expect("로그인 유저와 일치하는 모든 대표 여행일지 목록을 조회한다.") {
                repTravelJournalRepository.findSliceBy(
                    TEST_DEFAULT_SIZE,
                    null,
                    RepresentativeTravelJournalSortType.LATEST,
                    loginUser,
                ).size shouldBe 1
            }

            expect("타켓 유저와 일치하는 모든 대표 여행일지 목록을 조회한다.") {
                repTravelJournalRepository.findTargetSliceBy(
                    TEST_DEFAULT_SIZE,
                    null,
                    RepresentativeTravelJournalSortType.LATEST,
                    targetUser,
                    loginUser.id,
                ).size shouldBe 2
            }
        }
    },
)
