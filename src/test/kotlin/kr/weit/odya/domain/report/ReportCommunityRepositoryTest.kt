package kr.weit.odya.domain.report

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_OTHER_COMMUNITY_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityContentImage
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createCustomUser
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createReportCommunity
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class ReportCommunityRepositoryTest(
    private val reportCommunityRepository: ReportCommunityRepository,
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
        lateinit var reportCommunity1: ReportCommunity
        lateinit var reportCommunity2: ReportCommunity
        lateinit var reportCommunity3: ReportCommunity
        lateinit var contentImage1: ContentImage
        lateinit var contentImage2: ContentImage
        beforeEach {
            user1 = userRepository.save(createUser())
            user2 = userRepository.save(createOtherUser())
            user3 = userRepository.save(createCustomUser("test3", "test3"))
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
                    id = TEST_OTHER_COMMUNITY_ID,
                    user = user2,
                    travelJournal = travelJournal2,
                    communityContentImages =
                    listOf(createCommunityContentImage(contentImage2)),
                ),
            )
            reportCommunity1 = reportCommunityRepository.save(createReportCommunity(community2, user1))
            reportCommunity2 = reportCommunityRepository.save(createReportCommunity(community1, user2))
            reportCommunity3 = reportCommunityRepository.save(createReportCommunity(community2, user3))
        }

        context("커뮤니티 신고 여부 조회") {
            expect("COMMUNITY_ID와 USER_ID가 일치하는 신고된 커뮤니티 여부한다(존재함)") {
                val result = reportCommunityRepository.existsByCommunityIdAndUserId(community2.id, user1.id)
                result shouldBe true
            }

            expect("COMMUNITY_ID와 USER_ID가 일치하는 신고된 커뮤니티 여부한다(존재하지 않음)") {
                val result = reportCommunityRepository.existsByCommunityIdAndUserId(community1.id, user3.id)
                result shouldBe false
            }
        }

        context("커뮤니티 신고 수 조회") {
            expect("COMMUNITY_ID와 일치하는 커뮤니티의 신고 수를 조회한다") {
                val result = reportCommunityRepository.countAllByCommunityId(community2.id)
                result shouldBe 2
            }
        }

        context("커뮤니티 신고 삭제") {
            expect("COMMUNITY_ID와 일치하는 커뮤니티의 신고 모두 삭제한다") {
                reportCommunityRepository.deleteAllByCommunityId(community2.id)
                reportCommunityRepository.count() shouldBe 1L
            }

            expect("USER_ID와 일치하는 커뮤니티의 신고 모두 삭제한다") {
                reportCommunityRepository.deleteAllByUserId(user1.id)
                reportCommunityRepository.count() shouldBe 1
            }
        }
    },
)
