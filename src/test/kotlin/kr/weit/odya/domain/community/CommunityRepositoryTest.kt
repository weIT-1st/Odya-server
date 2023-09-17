package kr.weit.odya.domain.community

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_COMMUNITY_CONTENT
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
import kr.weit.odya.support.test.flushAndClear
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@RepositoryTest
class CommunityRepositoryTest(
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val tem: TestEntityManager,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var travelJournal: TravelJournal
        lateinit var community: Community

        beforeEach {
            user = userRepository.save(createUser())
            otherUser = userRepository.save(createOtherUser())
            travelJournal = travelJournalRepository.save(
                createTravelJournal(
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
            )
            community = communityRepository.save(
                createCommunity(
                    travelJournal = travelJournal,
                    user = user,
                    communityContentImages = listOf(
                        createCommunityContentImage(contentImage = createContentImage(user = user)),
                    ),
                ),
            )
        }

        context("커뮤니티 아이디") {
            expect("커뮤니티 ID와 일치하는 커뮤니티를 조회한다") {
                val result = communityRepository.getByCommunityId(community.id)
                result.content shouldBe TEST_COMMUNITY_CONTENT
            }
        }

        context("커뮤니티 수정") {
            expect("커뮤니티의 여행일지 ID 컬럼을 null로 수정한다") {
                communityRepository.updateTravelJournalIdToNull(travelJournal.id)
                tem.flushAndClear()
                val result = communityRepository.getByCommunityId(community.id)
                result.travelJournal shouldBe null
            }
        }
    },
)
