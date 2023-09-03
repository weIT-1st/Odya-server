package kr.weit.odya.domain.community

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityContentImage
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class CommunityRepositoryTest(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val contentImageRepository: ContentImageRepository,
) : ExpectSpec(
    {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var user3: User
        lateinit var community1: Community
        lateinit var community2: Community
        lateinit var travelJournal1: TravelJournal
        lateinit var travelJournal2: TravelJournal
        lateinit var contentImage1: ContentImage
        lateinit var contentImage2: ContentImage
        beforeEach {
            user1 = userRepository.save(createUser())
            user2 = userRepository.save(createOtherUser())
            contentImage1 = contentImageRepository.save(createContentImage(user = user1))
            contentImage2 = contentImageRepository.save(createContentImage(user = user2))
            travelJournal1 = travelJournalRepository.save(
                travelJournalRepository.save(
                    createTravelJournal(
                        user = user1,
                        travelCompanions = listOf(createTravelCompanionById(user = user2)),
                        travelJournalContents = listOf(
                            createTravelJournalContent(
                                travelJournalContentImages = listOf(
                                    createTravelJournalContentImage(contentImage = contentImage1),
                                ),
                            ),
                        ),
                    ),
                ),
            )
            travelJournal2 = travelJournalRepository.save(
                createTravelJournal(
                    id = 2L,
                    user = user2,
                    travelCompanions = listOf(createTravelCompanionById(user = user1)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(createTravelJournalContentImage(contentImage = contentImage2)),
                        ),
                    ),
                ),
            )
            community1 = communityRepository.save(
                createCommunity(
                    user = user1,
                    travelJournal = travelJournal1,
                    communityContentImages =
                    listOf(createCommunityContentImage(contentImage1)),
                ),
            )
            community2 = communityRepository.save(
                createCommunity(
                    user = user2,
                    travelJournal = travelJournal2,
                    communityContentImages =
                    listOf(createCommunityContentImage(contentImage2)),
                ),
            )
        }

        context("커뮤니티 조회") {
            expect("COMMUNITY_ID와 일치하는 커뮤니티 조회") {
                val result = communityRepository.getByCommunityId(community1.id)
                result shouldBe community1
            }
        }
    },
)
