package kr.weit.odya.support

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.community.CommunityInformation
import kr.weit.odya.domain.community.CommunityVisibility
import kr.weit.odya.domain.communitycomment.CommunityComment
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.CommunityContentImageResponse
import kr.weit.odya.service.dto.CommunityCreateRequest
import kr.weit.odya.service.dto.CommunityResponse
import kr.weit.odya.service.dto.CommunitySimpleResponse
import kr.weit.odya.service.dto.CommunitySummaryResponse
import kr.weit.odya.service.dto.CommunityUpdateRequest
import kr.weit.odya.service.dto.CommunityWithCommentsResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TopicResponse
import kr.weit.odya.service.dto.TravelJournalSimpleResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream
import java.time.LocalDateTime

const val TEST_COMMUNITY_ID = 1L
const val TEST_OTHER_COMMUNITY_ID = 2L
const val TEST_ANOTHER_COMMUNITY_ID = 3L
const val TEST_NOT_EXISTS_COMMUNITY_ID = 9999L
const val TEST_COMMUNITY_CONTENT = "테스트 커뮤니티 글입니다."
const val TEST_COMMUNITY_PLACE_ID = "테스트 장소 아이디"
const val TEST_UPDATE_COMMUNITY_CONTENT = "테스트 커뮤니티 글 업데이트입니다."
val TEST_COMMUNITY_VISIBILITY = CommunityVisibility.PUBLIC
val TEST_UPDATE_COMMUNITY_VISIBILITY = CommunityVisibility.FRIEND_ONLY
const val TEST_COMMUNITY_REQUEST_NAME = "community"
const val TEST_COMMUNITY_MOCK_FILE_NAME = "community-content-image"
const val TEST_COMMUNITY_CONTENT_IMAGE_DELETE_ID = 2L
const val TEST_COMMUNITY_CONTENT_IMAGE_ID = 1L
const val TEST_OTHER_COMMUNITY_CONTENT_IMAGE_ID = 1L
const val TEST_COMMUNITY_UPDATE_REQUEST_NAME = "update-community"
const val TEST_COMMUNITY_UPDATE_MOCK_FILE_NAME = "update-community-content-image"
const val MIM_COMMUNITY_CONTENT_IMAGE_COUNT = 1
const val MAX_COMMUNITY_CONTENT_IMAGE_COUNT = 15
const val TEST_COMMUNITY_LIKE_COUNT = 2
const val TEST_IS_USER_LIKED = false
val TEST_CREATED_DATE: LocalDateTime = LocalDateTime.parse("2021-01-01T00:00:00")

fun createCommunityCreateRequest(
    content: String = TEST_COMMUNITY_CONTENT,
    visibility: CommunityVisibility = TEST_COMMUNITY_VISIBILITY,
    placeId: String? = null,
    travelJournalId: Long? = null,
    topicId: Long? = null,
) = CommunityCreateRequest(
    content = content,
    visibility = visibility,
    placeId = placeId,
    travelJournalId = travelJournalId,
    topicId = topicId,
)

fun createCommunityRequestFile(
    name: String = TEST_COMMUNITY_REQUEST_NAME,
    originalFileName: String? = TEST_COMMUNITY_REQUEST_NAME,
    contentType: String? = TEST_FILE_CONTENT_TYPE,
    contentStream: InputStream,
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        contentStream,
    )
}

fun createCommunityContentImagePairs(): List<Pair<String, String>> = listOf(
    TEST_GENERATED_FILE_NAME to TEST_IMAGE_FILE_WEBP,
    TEST_GENERATED_FILE_NAME to TEST_OTHER_IMAGE_FILE_WEBP,
)

fun createUpdateCommunityContentImagePairs(): List<Pair<String, String>> = listOf(
    TEST_GENERATED_FILE_NAME to TEST_UPDATE_IMAGE_FILE_WEBP,
)

fun createCommunity(
    id: Long = TEST_COMMUNITY_ID,
    visibility: CommunityVisibility = TEST_COMMUNITY_VISIBILITY,
    topic: Topic? = createTopic(),
    travelJournal: TravelJournal? = createTravelJournal(),
    communityContentImages: List<CommunityContentImage> = listOf(
        createCommunityContentImage(),
        createCommunityContentImage(
            createOtherContentImage(),
        ),
    ),
    user: User = createUser(),
    placeId: String? = TEST_PLACE_ID,
) = Community(
    id = id,
    user = user,
    topic = topic,
    travelJournal = travelJournal,
    communityContentImages = communityContentImages,
    communityInformation = CommunityInformation(
        content = TEST_COMMUNITY_CONTENT,
        visibility = visibility,
        placeId = placeId,
    ),
)

fun createCommunityContentImage(contentImage: ContentImage = createContentImage()) = CommunityContentImage(
    contentImage = contentImage,
)

fun createAllCommunities() =
    listOf(createMockCommunity(), createMockCommunity(id = TEST_OTHER_COMMUNITY_ID, user = createOtherUser()))

fun createMyCommunities() = listOf(createMockCommunity(), createMockCommunity(id = TEST_OTHER_COMMUNITY_ID))

fun createFriendCommunities() =
    listOf(createMockCommunity(), createMockCommunity(id = TEST_OTHER_COMMUNITY_ID, user = createOtherUser()))

fun createTopicCommunities() =
    listOf(
        createMockCommunity(topic = createOtherTopic()),
        createMockCommunity(id = TEST_OTHER_COMMUNITY_ID, user = createOtherUser(), topic = createOtherTopic()),
    )

fun createCommunityUpdateRequest(
    content: String = TEST_UPDATE_COMMUNITY_CONTENT,
    visibility: CommunityVisibility = TEST_UPDATE_COMMUNITY_VISIBILITY,
    placeId: String? = TEST_UPDATE_PLACE_ID,
    travelJournalId: Long? = TEST_UPDATE_TRAVEL_JOURNAL_ID,
    topicId: Long? = TEST_UPDATE_TOPIC_ID,
    deleteCommunityContentImageIds: List<Long>? = listOf(TEST_COMMUNITY_CONTENT_IMAGE_DELETE_ID),
) = CommunityUpdateRequest(
    content = content,
    visibility = visibility,
    placeId = placeId,
    travelJournalId = travelJournalId,
    topicId = topicId,
    deleteCommunityContentImageIds = deleteCommunityContentImageIds,
)

fun createCommunityContentImageUpdatePairs(): List<Pair<String, String>> = listOf(
    TEST_GENERATED_FILE_NAME to TEST_UPDATE_IMAGE_FILE_WEBP,
)

fun createCommunityResponse(
    id: Long = TEST_COMMUNITY_ID,
    content: String = TEST_COMMUNITY_CONTENT,
    visibility: CommunityVisibility = TEST_COMMUNITY_VISIBILITY,
    placeId: String? = TEST_COMMUNITY_PLACE_ID,
    isWriter: Boolean = true,
    travelJournal: TravelJournalSimpleResponse? = createTravelJournalSimpleResponse(),
    topic: TopicResponse? = createCommunityTopicResponse(),
    communityContentImages: List<CommunityContentImageResponse> = listOf(createCommunityContentImageResponse()),
) = CommunityResponse(
    communityId = id,
    content = content,
    visibility = visibility,
    placeId = placeId,
    isWriter = isWriter,
    writer = UserSimpleResponse(createUser(), TEST_FILE_AUTHENTICATED_URL),
    travelJournal = travelJournal,
    topic = topic,
    communityContentImages = communityContentImages,
    communityCommentCount = TEST_COMMUNITY_COMMENT_COUNT,
    communityLikeCount = TEST_COMMUNITY_LIKE_COUNT,
    isUserLiked = TEST_IS_USER_LIKED,
    createdDate = TEST_CREATED_DATE,
)

fun createOtherCommunityResponse(
    id: Long = TEST_OTHER_COMMUNITY_ID,
    content: String = TEST_COMMUNITY_CONTENT,
    visibility: CommunityVisibility = TEST_COMMUNITY_VISIBILITY,
    placeId: String? = TEST_COMMUNITY_PLACE_ID,
    isWriter: Boolean = true,
    travelJournal: TravelJournalSimpleResponse? = createTravelJournalSimpleResponse(id = TEST_OTHER_TRAVEL_JOURNAL_ID),
    topic: TopicResponse? = createCommunityTopicResponse(id = TEST_OTHER_TOPIC_ID),
    communityContentImages: List<CommunityContentImageResponse> = listOf(createCommunityContentImageResponse(id = TEST_OTHER_COMMUNITY_CONTENT_IMAGE_ID)),
) = CommunityResponse(
    communityId = id,
    content = content,
    visibility = visibility,
    placeId = placeId,
    isWriter = isWriter,
    writer = UserSimpleResponse(createOtherUser(), TEST_FILE_AUTHENTICATED_URL),
    travelJournal = travelJournal,
    topic = topic,
    communityContentImages = communityContentImages,
    communityCommentCount = TEST_COMMUNITY_COMMENT_COUNT,
    communityLikeCount = TEST_COMMUNITY_COMMENT_COUNT,
    isUserLiked = TEST_IS_USER_LIKED,
    createdDate = TEST_CREATED_DATE,
)

fun createCommunityTopicResponse(
    id: Long = TEST_TOPIC_ID,
    word: String = TEST_TOPIC,
) = TopicResponse(
    id = id,
    topic = word,
)

fun createCommunityContentImageResponse(
    id: Long = TEST_COMMUNITY_CONTENT_IMAGE_ID,
    imageUrl: String = TEST_FILE_AUTHENTICATED_URL,
) = CommunityContentImageResponse(
    communityContentImageId = id,
    imageUrl = imageUrl,
)

fun createCommunitySummaryResponse(
    community: Community = createMockCommunity(
        id = TEST_COMMUNITY_ID,
        communityContentImages = listOf(createCommunityContentImage()),
    ),
    communityMainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
    writerProfileUrl: String = TEST_FILE_AUTHENTICATED_URL,
    isFollowing: Boolean = true,
    communityCommentCount: Int = TEST_COMMUNITY_COMMENT_COUNT,
) = CommunitySummaryResponse.from(
    community = community,
    communityMainImageUrl = communityMainImageUrl,
    writerProfileUrl = writerProfileUrl,
    isFollowing = isFollowing,
    communityCommentCount = communityCommentCount,
)

fun createOtherCommunitySummaryResponse(
    community: Community = createMockCommunity(
        id = TEST_OTHER_COMMUNITY_ID,
        communityContentImages = listOf(createCommunityContentImage()),
        placeId = TEST_OTHER_PLACE_ID,
    ),
    communityMainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
    writerProfileUrl: String = TEST_FILE_AUTHENTICATED_URL,
    isFollowing: Boolean = true,
    communityCommentCount: Int = TEST_COMMUNITY_COMMENT_COUNT,
) = CommunitySummaryResponse.from(
    community = community,
    communityMainImageUrl = communityMainImageUrl,
    writerProfileUrl = writerProfileUrl,
    isFollowing = isFollowing,
    communityCommentCount = communityCommentCount,
)

fun createCommunitySimpleResponse(
    id: Long = TEST_COMMUNITY_ID,
    mainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
) = CommunitySimpleResponse(
    communityId = id,
    communityMainImageUrl = mainImageUrl,
    placeId = TEST_PLACE_ID,
)

fun createOtherCommunitySimpleResponse(
    id: Long = TEST_OTHER_COMMUNITY_ID,
    mainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
) = CommunitySimpleResponse(
    communityId = id,
    communityMainImageUrl = mainImageUrl,
    placeId = TEST_OTHER_PLACE_ID,
)

fun createCommunityWithCommentResponse(
    community: Community = createMockCommunity(
        id = TEST_COMMUNITY_ID,
        communityContentImages = listOf(createCommunityContentImage()),
    ),
    communityMainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
    writerProfileUrl: String = TEST_FILE_AUTHENTICATED_URL,
    communityComment: CommunityComment = createMockCommunityComment(),
    commenterProfileUrl: String = TEST_FILE_AUTHENTICATED_URL,
) = CommunityWithCommentsResponse.from(
    community = community,
    communityMainImageUrl = communityMainImageUrl,
    writerProfileUrl = writerProfileUrl,
    communityComment = communityComment,
    commenterProfileUrl = commenterProfileUrl,
)

fun createOtherCommunityWithCommentResponse(
    community: Community = createMockCommunity(
        id = TEST_OTHER_COMMUNITY_ID,
        communityContentImages = listOf(createCommunityContentImage()),
    ),
    communityMainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
    writerProfileUrl: String = TEST_FILE_AUTHENTICATED_URL,
    communityComment: CommunityComment = createMockCommunityComment(),
    commenterProfileUrl: String = TEST_FILE_AUTHENTICATED_URL,
) = CommunityWithCommentsResponse.from(
    community = community,
    communityMainImageUrl = communityMainImageUrl,
    writerProfileUrl = writerProfileUrl,
    communityComment = communityComment,
    commenterProfileUrl = commenterProfileUrl,
)

fun createSliceCommunitySummaryResponse(
    hasNext: Boolean = false,
    content: List<CommunitySummaryResponse> = listOf(
        createCommunitySummaryResponse(),
        createOtherCommunitySummaryResponse(),
    ),
) = SliceResponse(
    hasNext = hasNext,
    content = content,
)

fun createSliceCommunitySimpleResponse(
    hasNext: Boolean = false,
    content: List<CommunitySimpleResponse> = listOf(
        createCommunitySimpleResponse(),
        createOtherCommunitySimpleResponse(),
    ),
) = SliceResponse(
    hasNext = hasNext,
    content = content,
)

fun createSliceCommunityWithCommentResponse(
    hasNext: Boolean = false,
    content: List<CommunityWithCommentsResponse> = listOf(
        createCommunityWithCommentResponse(),
        createOtherCommunityWithCommentResponse(),
    ),
) = SliceResponse(
    hasNext = hasNext,
    content = content,
)

fun createMockCommunity(
    id: Long = TEST_COMMUNITY_ID,
    visibility: CommunityVisibility = TEST_COMMUNITY_VISIBILITY,
    communityContentImages: List<CommunityContentImage> = listOf(
        createCommunityContentImage(),
        createCommunityContentImage(
            createOtherContentImage(),
        ),
    ),
    user: User = createUser(),
    placeId: String? = TEST_PLACE_ID,
    topic: Topic? = createTopic(),
): MockCommunity = MockCommunity(id, visibility, communityContentImages, user, placeId, topic)

class MockCommunity(
    id: Long = TEST_COMMUNITY_ID,
    visibility: CommunityVisibility = TEST_COMMUNITY_VISIBILITY,
    communityContentImages: List<CommunityContentImage> = listOf(
        createCommunityContentImage(),
        createCommunityContentImage(
            createOtherContentImage(),
        ),
    ),
    user: User = createUser(),
    placeId: String? = TEST_PLACE_ID,
    topic: Topic? = createTopic(),
    travelJournal: TravelJournal? = createTravelJournal(),
) : Community(
    id = id,
    user = user,
    communityContentImages = communityContentImages,
    communityInformation = CommunityInformation(
        content = TEST_COMMUNITY_CONTENT,
        visibility = visibility,
        placeId = placeId,
    ),
    travelJournal = travelJournal,
    topic = topic,
) {
    override var updatedDate: LocalDateTime = LocalDateTime.of(2023, 9, 1, 0, 0, 0)
    override var createdDate: LocalDateTime = LocalDateTime.of(2023, 9, 1, 0, 0, 0)
}
