package kr.weit.odya.support

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.communitycomment.CommunityComment
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.CommunityCommentRequest
import kr.weit.odya.service.dto.CommunityCommentResponse
import kr.weit.odya.service.dto.SliceResponse
import java.time.LocalDateTime

const val TEST_COMMUNITY_COMMENT_ID: Long = 1L
const val TEST_OTHER_COMMUNITY_COMMENT_ID: Long = 2L
const val TEST_COMMUNITY_COMMENT_CONTENT = "testCommunityCommentContent"
const val TEST_COMMUNITY_COMMENT_COUNT = 5

fun createCommunityComment(
    id: Long = TEST_COMMUNITY_COMMENT_ID,
    user: User = createUser(),
    community: Community = createCommunity(),
): CommunityComment =
    CommunityComment(
        id = id,
        content = TEST_COMMUNITY_COMMENT_CONTENT,
        user = user,
        community = community,
    )

fun createMockCommunityComment(
    id: Long = TEST_COMMUNITY_COMMENT_ID,
    user: User = createUser(),
    community: Community = createMockCommunity(),
): MockCommunityComment = MockCommunityComment(id, user, community)

fun createOtherMockCommunityComment(
    id: Long = TEST_OTHER_COMMUNITY_COMMENT_ID,
    user: User = createUser(),
    community: Community = createMockCommunity(id = TEST_OTHER_COMMUNITY_ID),
): MockCommunityComment = MockCommunityComment(id, user, community)

fun createCommunitiesComments(): List<CommunityComment> =
    listOf(
        createMockCommunityComment(),
        createOtherMockCommunityComment(),
    )

fun createCommunityCommentRequest(): CommunityCommentRequest =
    CommunityCommentRequest(content = TEST_COMMUNITY_COMMENT_CONTENT)

fun createCommunityCommentSliceResponse(): SliceResponse<CommunityCommentResponse> =
    SliceResponse(
        TEST_DEFAULT_SIZE,
        listOf(
            createCommunityCommentResponse(),
            createCommunityCommentResponse(
                id = TEST_OTHER_COMMUNITY_COMMENT_ID,
                user = createOtherUser(),
            ),
        ),
    )

fun createCommunityCommentResponse(
    id: Long = TEST_COMMUNITY_COMMENT_ID,
    user: User = createUser(),
    communityComment: MockCommunityComment = createMockCommunityComment(id = id, user = user),
): CommunityCommentResponse =
    CommunityCommentResponse(communityComment, user, TEST_FILE_AUTHENTICATED_URL)

class MockCommunityComment(
    id: Long = TEST_COMMUNITY_COMMENT_ID,
    user: User = createUser(),
    community: Community = createCommunity(),
) : CommunityComment(
    id = id,
    content = TEST_COMMUNITY_COMMENT_CONTENT,
    user = user,
    community = community,
) {
    override var updatedDate: LocalDateTime = LocalDateTime.of(2023, 9, 4, 0, 0, 0)
}
