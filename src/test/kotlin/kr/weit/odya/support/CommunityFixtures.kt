package kr.weit.odya.support

import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.community.CommunityVisibility
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.CommunityCreateRequest
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream

const val TEST_COMMUNITY_CONTENT = "테스트 커뮤니티 글입니다."
val TEST_COMMUNITY_VISIBILITY = CommunityVisibility.PUBLIC
const val TEST_COMMUNITY_REQUEST_NAME = "community"
const val TEST_COMMUNITY_MOCK_FILE_NAME = "community-content-image"

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

fun createCommunity(
    topic: Topic? = createTopic(),
    travelJournal: TravelJournal? = createTravelJournal(),
    communityContentImages: List<CommunityContentImage> = listOf(
        createCommunityContentImage(),
        createCommunityContentImage(
            createOtherContentImage(),
        ),
    ),
    user: User = createUser(),
) = Community(
    content = TEST_COMMUNITY_CONTENT,
    visibility = TEST_COMMUNITY_VISIBILITY,
    user = user,
    topic = topic,
    travelJournal = travelJournal,
    communityContentImages = communityContentImages,
)

fun createCommunityContentImage(contentImage: ContentImage = createContentImage()) = CommunityContentImage(
    contentImage = contentImage,
)
