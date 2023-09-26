package kr.weit.odya.domain.community

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.follow.Follow
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_COMMUNITY_CONTENT
import kr.weit.odya.support.TEST_GENERATED_FILE_NAME
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
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val contentImageRepository: ContentImageRepository,
    private val tem: TestEntityManager,
) : ExpectSpec(
    {
        lateinit var user1: User
        lateinit var user2: User
        lateinit var travelJournal1: TravelJournal
        lateinit var travelJournal2: TravelJournal
        lateinit var community1: Community
        lateinit var community2: Community
        lateinit var community3: Community
        beforeEach {
            user1 = userRepository.save(createUser())
            user2 = userRepository.save(createOtherUser())
            followRepository.save(Follow(user1, user2))
            val contentImage1 = createContentImage(user = user1)
            val contentImage2 = createContentImage(user = user2)
            val contentImage3 = createContentImage(user = user1)
            travelJournal1 = travelJournalRepository.save(
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
                    listOf(createCommunityContentImage(contentImage1), createCommunityContentImage(contentImage3)),
                ),
            )
            community2 = communityRepository.save(
                createCommunity(
                    user = user2,
                    travelJournal = travelJournal2,
                    communityContentImages = listOf(createCommunityContentImage(contentImage2)),
                ),
            )
            community3 = communityRepository.save(
                createCommunity(
                    user = user2,
                    travelJournal = null,
                    visibility = CommunityVisibility.FRIEND_ONLY,
                    communityContentImages = listOf(createCommunityContentImage(contentImage2)),
                ),
            )
        }

        context("커뮤니티 조회") {
            expect("커뮤니티 ID와 일치하는 커뮤니티를 조회한다") {
                val result = communityRepository.getByCommunityId(community1.id)
                result.content shouldBe TEST_COMMUNITY_CONTENT
            }
        }

        context("커뮤니티 목록 조회") {
            expect("나와 친구인 사용자의 친구만 공개 여행 일지 목록을 조회한다.") {
                val result =
                    communityRepository.getCommunitySliceBy(user1.id, 10, null, CommunitySortType.LATEST)
                result.size shouldBe 3
            }

            expect("나의 여행 일지 목록을 조회한다.") {
                val result =
                    communityRepository.getMyCommunitySliceBy(user1.id, 10, null, CommunitySortType.LATEST)
                result.size shouldBe 1
            }

            expect("나와 친구인 사용자의 여행 일지 목록을 조회한다.") {
                val result =
                    communityRepository.getFriendCommunitySliceBy(user1.id, 10, null, CommunitySortType.LATEST)
                result.size shouldBe 2
            }
        }

        context("커뮤니티 수정") {
            expect("커뮤니티의 여행일지 ID 컬럼을 null로 수정한다") {
                communityRepository.updateTravelJournalIdToNull(travelJournal1.id)
                tem.flushAndClear()
                val result = communityRepository.getByCommunityId(community1.id)
                result.travelJournal shouldBe null
            }
        }

        context("커뮤니티 이미지") {
            expect("커뮤니티 ID와 일치하는 커뮤니티의 이미지 이름을 조회한다") {
                val result = communityRepository.getImageNamesById(community1.id)
                result shouldBe listOf(TEST_GENERATED_FILE_NAME, TEST_GENERATED_FILE_NAME)
            }
        }

        context("커뮤니티 삭제") {
            expect("유저 ID와 일치하는 커뮤니티를 삭제한다") {
                communityRepository.deleteAllByUserId(user1.id)
                communityRepository.findAll().size shouldBe 2
            }
        }
    },
)
