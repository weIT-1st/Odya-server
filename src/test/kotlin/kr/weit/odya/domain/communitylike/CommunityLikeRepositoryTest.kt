package kr.weit.odya.domain.communitylike

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_OTHER_COMMUNITY_ID
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityContentImage
import kr.weit.odya.support.createCommunityLike
import kr.weit.odya.support.createContentImage
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createTravelCompanionById
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createTravelJournalContent
import kr.weit.odya.support.createTravelJournalContentImage
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class CommunityLikeRepositoryTest(
    private val communityLikeRepository: CommunityLikeRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var travelJournal: TravelJournal
        lateinit var community1: Community
        lateinit var community2: Community
        beforeEach {
            user = userRepository.save(createUser())
            otherUser = userRepository.save(createOtherUser())
            val contentImage = createContentImage(user = user)
            travelJournal = travelJournalRepository.save(
                createTravelJournal(
                    user = user,
                    travelCompanions = listOf(createTravelCompanionById(user = otherUser)),
                    travelJournalContents = listOf(
                        createTravelJournalContent(
                            travelJournalContentImages = listOf(
                                createTravelJournalContentImage(contentImage = contentImage),
                            ),
                        ),
                    ),
                ),
            )
            community1 = communityRepository.save(
                createCommunity(
                    travelJournal = travelJournal,
                    user = user,
                    communityContentImages = listOf(
                        createCommunityContentImage(contentImage = createContentImage(user = user)),
                    ),
                ),
            )
            community2 = communityRepository.save(
                createCommunity(
                    id = TEST_OTHER_COMMUNITY_ID,
                    travelJournal = null,
                    user = otherUser,
                    communityContentImages = listOf(
                        createCommunityContentImage(contentImage = createContentImage(user = otherUser)),
                    ),
                ),
            )
            communityLikeRepository.save(createCommunityLike(community1, user))
            communityLikeRepository.save(createCommunityLike(community2, user))
        }

        context("좋아요 개수 조회") {
            expect("유저 ID와 일치하는 커뮤니티의 좋아요 개수를 조회한다") {
                val communityLikeCount = communityLikeRepository.countByUserId(user.id)
                communityLikeCount shouldBe 2
            }
        }

        context("좋아요 삭제") {
            expect("커뮤니티 ID와 일치하는 커뮤니티의 좋아요를 삭제한다") {
                communityLikeRepository.deleteAllByCommunityId(community1.id)
                val communityLikeCount = communityLikeRepository.countByUserId(user.id)
                communityLikeCount shouldBe 1
            }

            expect("유저 ID와 일치하는 커뮤니티의 좋아요를 삭제한다") {
                communityLikeRepository.deleteCommunityLikes(user.id)
                val communityLikeCount = communityLikeRepository.countByUserId(user.id)
                communityLikeCount shouldBe 0
            }
        }

        context("좋아요 여부 확인") {
            expect("커뮤니티 ID, 유저 ID와 일치하는 커뮤니티의 좋아요가 존재하는지 확인한다") {
                val isUserLiked = communityLikeRepository.existsByCommunityIdAndUserId(community1.id, user.id)
                isUserLiked shouldBe true
            }
        }

        context("좋아요한 커뮤니티 전체 조회") {
            expect("유저 ID와 일치하는 커뮤니티 목록을 조회한다") {
                val result = communityLikeRepository.getLikedCommunitySliceBy(user.id, 10, null)
                result.size shouldBe 2
            }
        }
    },
)
