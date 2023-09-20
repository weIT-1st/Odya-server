package kr.weit.odya.domain.communitycomment

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_COMMUNITY_COMMENT_CONTENT
import kr.weit.odya.support.TEST_DEFAULT_SIZE
import kr.weit.odya.support.createCommunity
import kr.weit.odya.support.createCommunityComment
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
class CommunityCommentRepositoryTest(
    private val communityCommentRepository: CommunityCommentRepository,
    private val userRepository: UserRepository,
    private val communityRepository: CommunityRepository,
    private val travelJournalRepository: TravelJournalRepository,
) : ExpectSpec(
    {
        lateinit var user: User
        lateinit var otherUser: User
        lateinit var travelJournal: TravelJournal
        lateinit var community: Community
        lateinit var communityComment1: CommunityComment
        lateinit var communityComment2: CommunityComment

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
            communityComment1 =
                communityCommentRepository.save(createCommunityComment(user = user, community = community))
            communityComment2 =
                communityCommentRepository.save(createCommunityComment(user = user, community = community))
        }

        context("커뮤니티 댓글 조회") {
            expect("커뮤니티 댓글 ID와 커뮤니티 ID가 일치하는 커뮤니티 댓글을 조회한다.") {
                val result = communityCommentRepository.getCommunityCommentBy(communityComment1.id, community.id)
                result.content shouldBe TEST_COMMUNITY_COMMENT_CONTENT
            }
        }

        context("커뮤니티 댓글 목록") {
            expect("커뮤니티 ID와 조건에 일치하는 커뮤니티 댓글 목록을 조회한다.") {
                val result =
                    communityCommentRepository.getSliceCommunityCommentBy(
                        community.id,
                        TEST_DEFAULT_SIZE,
                        communityComment1.id,
                    )
                result.size shouldBe 1
            }
        }

        context("커뮤니티 댓글 개수 조회") {
            expect("커뮤니티 ID와 조건에 일치하는 커뮤니티 댓글 개수를 조회한다.") {
                val result = communityCommentRepository.countByCommunityId(community.id)
                result shouldBe 2
            }
        }
    },
)
