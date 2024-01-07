package kr.weit.odya.domain.traveljournal

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TravelCompanionRepositoryTest(
    private val userRepository: UserRepository,
    private val travelCompanionRepository: TravelCompanionRepository,
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var otherUser2: User
        lateinit var travelCompanion1: TravelCompanion
        lateinit var travelCompanion2: TravelCompanion

        beforeEach {
            user = userRepository.save(createUser())
            otherUser = userRepository.save(createOtherUser())
            otherUser2 = userRepository.save(createCustomUser("testUser3", "testUser3"))
            travelCompanion1 = TravelCompanion(0L, otherUser, otherUser.username)
            travelCompanion2 = TravelCompanion(0L, otherUser2, otherUser2.username)
            travelJournalRepository.save(
                createTravelJournal(
                    user = user,
                    travelCompanions = listOf(
                        travelCompanion1,
                        travelCompanion2,
                    ),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = createContentImage(user = user)),
                            ),
                        ),
                    ),
                ),
            )
        }

        context("TravelCompanion 삭제") {
            expect("UserId와 일치하는 TravelCompanion 전부 삭제한다.") {
                travelCompanionRepository.count() shouldBe 2L
                travelCompanionRepository.deleteAllByUserId(otherUser2.id)
                travelCompanionRepository.count() shouldBe 1L
            }
        }
    },
)
