package kr.weit.odya.support

import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.traveljournal.Coordinates
import kr.weit.odya.domain.traveljournal.TravelCompanion
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentImage
import kr.weit.odya.domain.traveljournal.TravelJournalContentInformation
import kr.weit.odya.domain.traveljournal.TravelJournalInformation
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TaggedTravelJournalResponse
import kr.weit.odya.service.dto.TravelCompanionResponse
import kr.weit.odya.service.dto.TravelCompanionSimpleResponse
import kr.weit.odya.service.dto.TravelJournalContentImageResponse
import kr.weit.odya.service.dto.TravelJournalContentRequest
import kr.weit.odya.service.dto.TravelJournalContentResponse
import kr.weit.odya.service.dto.TravelJournalContentUpdateRequest
import kr.weit.odya.service.dto.TravelJournalRequest
import kr.weit.odya.service.dto.TravelJournalResponse
import kr.weit.odya.service.dto.TravelJournalSimpleResponse
import kr.weit.odya.service.dto.TravelJournalSummaryResponse
import kr.weit.odya.service.dto.TravelJournalUpdateRequest
import kr.weit.odya.service.dto.TravelJournalVisibilityUpdateRequest
import kr.weit.odya.service.dto.UserSimpleResponse
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream
import java.time.LocalDate

const val TEST_TRAVEL_JOURNAL_CONTENT_IMAGE_ID = 1L
const val TEST_OTHER_TRAVEL_JOURNAL_CONTENT_IMAGE_ID = 2L
const val TEST_TRAVEL_JOURNAL_ID = 1L
const val TEST_ALREADY_REGISTERED_REP_TRAVEL_JOURNAL_ID = 999L
const val TEST_TRAVEL_JOURNAL_CONTENT_ID = 1L
const val TEST_OTHER_TRAVEL_JOURNAL_CONTENT_ID = 2L
const val TEST_TRAVEL_JOURNAL_NOT_EXIST_CONTENT_ID = 99999L
const val TEST_PRIVATE_TRAVEL_JOURNAL_ID = 2L
const val TEST_UPDATE_TRAVEL_JOURNAL_ID = 3L
const val TEST_OTHER_TRAVEL_JOURNAL_ID = 4L
const val TEST_NOT_EXIST_TRAVEL_JOURNAL_ID = 5L
const val TEST_INVALID_TRAVEL_JOURNAL_ID = -1L
const val TEST_TRAVEL_JOURNAL_TITLE = "testTitle"
const val TEST_OTHER_TRAVEL_JOURNAL_TITLE = "testTitle2"
const val TEST_TRAVEL_JOURNAL_CONTENT = "testContent"
const val TEST_OTHER_TRAVEL_JOURNAL_CONTENT = "testContent2"
const val TEST_TRAVEL_COMPANION_NAME = "testCompanion"
const val TEST_TRAVEL_JOURNAL_COUNT = 1
const val TEST_TRAVEL_PLACE_COUNT = 2
val TEST_TRAVEL_JOURNAL_START_DATE: LocalDate = LocalDate.parse("2021-01-01")
val TEST_TRAVEL_JOURNAL_END_DATE: LocalDate = LocalDate.parse("2021-01-02")
val TEST_TRAVEL_DATE: LocalDate = LocalDate.parse("2021-01-01")
val TEST_OTHER_TRAVEL_DATE: LocalDate = LocalDate.parse("2021-01-02")
val TEST_TRAVEL_JOURNAL_LATITUDES = listOf(1.1111, 2.2222, 3.3333)
val TEST_TRAVEL_JOURNAL_LONGITUDES = listOf(1.1111, 2.2222, 3.3333)
val TEST_TRAVEL_COMPANION_IDS = listOf(TEST_OTHER_USER_ID)
val TEST_TRAVEL_COMPANION_NAMES = listOf(TEST_TRAVEL_COMPANION_NAME)
val TEST_TRAVEL_COMPANION_USERS = listOf(createOtherUser())
val TEST_TRAVEL_COMPANIONS = listOf(createTravelCompanionById(), createTravelCompanionByName())
const val TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME = "travel-journal-content-image"
const val TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_MOCK_FILE_NAME = "travel-journal-content-image-update"
val TEST_TRAVEL_CONTENT_IMAGE_MAP = createImageMap(TEST_TRAVEL_JOURNAL_MOCK_FILE_NAME)
val TEST_TRAVEL_JOURNAL = createTravelJournal()
const val TEST_TRAVEL_JOURNAL_REQUEST_NAME = "travel-journal"
const val TEST_TRAVEL_JOURNAL_UPDATE_REQUEST_NAME = "travel-journal-update"
const val TEST_TRAVEL_JOURNAL_CONTENT_UPDATE_REQUEST_NAME = "travel-journal-content-update"
const val MAX_TRAVEL_DAYS = 15
const val MAX_TRAVEL_COMPANION_COUNT = 15
const val TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_COUNT = 2
const val MAX_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT = 15
const val TEST_TRAVEL_JOURNAL_CONTENT_INCORRECT_IMAGE_NAME = "1.webp"
val TEST_TRAVEL_JOURNAL_INCORRECT_DATE: LocalDate = LocalDate.of(2023, 1, 1)
const val TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE = "updateTestImage.webp"
const val TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE = "updateTestImage2.webp"

fun createTravelJournalRequest(
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    travelStartDate: LocalDate = TEST_TRAVEL_JOURNAL_START_DATE,
    travelEndDate: LocalDate = TEST_TRAVEL_JOURNAL_END_DATE,
    visibility: TravelJournalVisibility = TravelJournalVisibility.PUBLIC,
    travelCompanionIds: List<Long>? = TEST_TRAVEL_COMPANION_IDS,
    travelCompanionNames: List<String>? = TEST_TRAVEL_COMPANION_NAMES,
    travelJournalContentRequests: List<TravelJournalContentRequest> = listOf(
        createTravelJournalContentRequest(),
        createOtherTravelJournalContentRequest(),
    ),
) = TravelJournalRequest(
    title = title,
    travelStartDate = travelStartDate,
    travelEndDate = travelEndDate,
    visibility = visibility,
    travelCompanionIds = travelCompanionIds,
    travelCompanionNames = travelCompanionNames,
    travelJournalContentRequests = travelJournalContentRequests,
)

fun createTravelJournalRequestByContentSize(size: Int) = createTravelJournalRequest(
    travelJournalContentRequests = List(size) { createTravelJournalContentRequest() },
)

fun createTravelJournalContentRequest(
    content: String? = TEST_TRAVEL_JOURNAL_CONTENT,
    latitudes: List<Double>? = TEST_TRAVEL_JOURNAL_LATITUDES,
    longitudes: List<Double>? = TEST_TRAVEL_JOURNAL_LONGITUDES,
    placeId: String? = TEST_PLACE_ID,
    travelDate: LocalDate = TEST_TRAVEL_DATE,
    contentImageNames: List<String> = listOf(TEST_IMAGE_FILE_WEBP),
) = TravelJournalContentRequest(
    content = content,
    latitudes = latitudes,
    longitudes = longitudes,
    placeId = placeId,
    travelDate = travelDate,
    contentImageNames = contentImageNames,
)

fun createTravelJournalContentRequestByImageNameSize(size: Int) = createTravelJournalContentRequest(
    contentImageNames = List(size) { "$it$TEST_IMAGE_FILE_WEBP" },
)

fun createOtherTravelJournalContentRequest() = createTravelJournalContentRequest(
    content = TEST_OTHER_TRAVEL_JOURNAL_CONTENT,
    travelDate = TEST_OTHER_TRAVEL_DATE,
    contentImageNames = listOf(TEST_OTHER_IMAGE_FILE_WEBP),
)

fun createTravelJournalVisibilityUpdateRequest(
    visibility: TravelJournalVisibility = TravelJournalVisibility.FRIEND_ONLY,
) = TravelJournalVisibilityUpdateRequest(
    visibility = visibility,
)

fun createImageMap(
    mockFileName: String,
    fileName: String = TEST_IMAGE_FILE_WEBP,
    otherFileName: String = TEST_OTHER_IMAGE_FILE_WEBP,
) = mapOf(
    fileName to createMockImageFile(mockFileName),
    otherFileName to createMockOtherImageFile(mockFileName),
)

fun createImageNamePairs() = listOf(
    TEST_GENERATED_FILE_NAME to TEST_IMAGE_FILE_WEBP,
    TEST_GENERATED_FILE_NAME to TEST_OTHER_IMAGE_FILE_WEBP,
)

fun createUpdateImageNamePairs() = listOf(
    TEST_GENERATED_FILE_NAME to TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
    TEST_GENERATED_FILE_NAME to TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
)

fun createTravelCompanionById(user: User = createOtherUser()) = TravelCompanion(
    user = user,
    username = null,
)

fun createTravelCompanionByName(name: String = TEST_TRAVEL_COMPANION_NAME) = TravelCompanion(
    user = null,
    username = name,
)

fun createTravelJournal(
    id: Long = TEST_TRAVEL_JOURNAL_ID,
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    travelStartDate: LocalDate = TEST_TRAVEL_JOURNAL_START_DATE,
    travelEndDate: LocalDate = TEST_TRAVEL_JOURNAL_END_DATE,
    visibility: TravelJournalVisibility = TravelJournalVisibility.PUBLIC,
    user: User = createUser(),
    travelCompanions: List<TravelCompanion> = TEST_TRAVEL_COMPANIONS,
    travelJournalContents: List<TravelJournalContent> = listOf(
        createTravelJournalContent(),
        createTravelJournalContent(
            id = TEST_OTHER_TRAVEL_JOURNAL_CONTENT_ID,
            travelJournalContentImages = listOf(createOtherTravelJournalContentImage(createOtherContentImage())),
        ),
    ),
) = TravelJournal(
    id = id,
    user = user,
    travelCompanions = travelCompanions,
    travelJournalContents = travelJournalContents,
    travelJournalInformation = TravelJournalInformation(
        title = title,
        travelStartDate = travelStartDate,
        travelEndDate = travelEndDate,
        visibility = visibility,
    ),
)

fun createPrivateTravelJournal(
    id: Long = TEST_PRIVATE_TRAVEL_JOURNAL_ID,
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    travelStartDate: LocalDate = TEST_TRAVEL_JOURNAL_START_DATE,
    travelEndDate: LocalDate = TEST_TRAVEL_JOURNAL_END_DATE,
    visibility: TravelJournalVisibility = TravelJournalVisibility.PRIVATE,
    user: User = createOtherUser(),
    travelCompanions: List<TravelCompanion> = TEST_TRAVEL_COMPANIONS,
    travelJournalContents: List<TravelJournalContent> = listOf(
        createTravelJournalContent(),
        createTravelJournalContent(
            travelJournalContentImages = listOf(createTravelJournalContentImage(createOtherContentImage())),
        ),
    ),
) = TravelJournal(
    id = id,
    user = user,
    travelCompanions = travelCompanions,
    travelJournalContents = travelJournalContents,
    travelJournalInformation = TravelJournalInformation(
        title = title,
        travelStartDate = travelStartDate,
        travelEndDate = travelEndDate,
        visibility = visibility,
    ),
)

fun createTravelJournalContent(
    id: Long = TEST_TRAVEL_JOURNAL_CONTENT_ID,
    content: String = TEST_TRAVEL_JOURNAL_CONTENT,
    placeId: String = TEST_PLACE_ID,
    latitudes: List<Double> = TEST_TRAVEL_JOURNAL_LATITUDES,
    longitudes: List<Double> = TEST_TRAVEL_JOURNAL_LONGITUDES,
    travelDate: LocalDate = TEST_TRAVEL_DATE,
    travelJournalContentImages: List<TravelJournalContentImage>? = listOf(
        createTravelJournalContentImage(),
    ),
) = TravelJournalContent(
    id = id,
    travelJournalContentImages = travelJournalContentImages ?: emptyList(),
    travelJournalContentInformation = TravelJournalContentInformation(
        content = content,
        placeId = placeId,
        coordinates = Coordinates(
            latitudes = latitudes,
            longitudes = longitudes,
        ),
        travelDate = travelDate,
    ),
)

fun createTravelJournalContentImage(contentImage: ContentImage = createContentImage()) = TravelJournalContentImage(
    id = TEST_TRAVEL_JOURNAL_CONTENT_IMAGE_ID,
    contentImage = contentImage,
)

fun createOtherTravelJournalContentImage(contentImage: ContentImage = createContentImage()) = TravelJournalContentImage(
    id = TEST_OTHER_TRAVEL_JOURNAL_CONTENT_IMAGE_ID,
    contentImage = contentImage,
)

fun createTravelJournalRequestFile(
    name: String = TEST_TRAVEL_JOURNAL_REQUEST_NAME,
    originalFileName: String? = TEST_TRAVEL_JOURNAL_REQUEST_NAME,
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

fun createTravelJournalByTravelCompanionIdSize(size: Int) = createTravelJournalRequest(
    travelCompanionIds = List(size) { it.toLong() },
)

fun createTravelJournalUpdateRequest(
    title: String = "updateTestTitle",
    travelStartDate: LocalDate = LocalDate.of(2021, 1, 1),
    travelEndDate: LocalDate = LocalDate.of(2021, 1, 10),
    visibility: TravelJournalVisibility = TravelJournalVisibility.PUBLIC,
    travelCompanionIds: List<Long>? = listOf(TEST_ANOTHER_USER_ID),
    travelCompanionNames: List<String>? = listOf("updateTestCompanion"),
) = TravelJournalUpdateRequest(
    title = title,
    travelStartDate = travelStartDate,
    travelEndDate = travelEndDate,
    visibility = visibility,
    travelCompanionIds = travelCompanionIds,
    travelCompanionNames = travelCompanionNames,
)

fun createTravelJournalContentUpdateRequest(
    content: String = "updateTestContent",
    placeId: String = TEST_PLACE_ID,
    latitudes: List<Double> = listOf(1.1111, 2.2222, 3.3333),
    longitudes: List<Double> = listOf(1.1111, 2.2222, 3.3333),
    travelDate: LocalDate = LocalDate.of(2021, 1, 1),
    updateContentImageNames: List<String> = listOf(
        TEST_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
        TEST_OTHER_UPDATE_TRAVEL_JOURNAL_CONTENT_IMAGE,
    ),
    deleteContentImageIds: List<Long> = listOf(1L),
) = TravelJournalContentUpdateRequest(
    content = content,
    placeId = placeId,
    latitudes = latitudes,
    longitudes = longitudes,
    travelDate = travelDate,
    updateContentImageNames = updateContentImageNames,
    deleteContentImageIds = deleteContentImageIds,
)

fun createSliceTravelJournalResponse(
    user: User = createUser(),
    travelCompanion: TravelCompanion = createTravelCompanionById(),
): SliceResponse<TravelJournalSummaryResponse> =
    SliceResponse(
        size = TEST_DEFAULT_SIZE,
        content = listOf(createTravelJournalSummaryResponse(user = user, travelCompanion = travelCompanion)),
    )

fun createTravelJournalSummaryResponse(
    user: User = createUser(),
    travelCompanion: TravelCompanion,
): TravelJournalSummaryResponse = TravelJournalSummaryResponse(
    TEST_TRAVEL_JOURNAL_ID,
    TEST_TRAVEL_JOURNAL_TITLE,
    TEST_TRAVEL_JOURNAL_CONTENT,
    TEST_FILE_AUTHENTICATED_URL,
    TEST_TRAVEL_JOURNAL_START_DATE,
    TEST_TRAVEL_JOURNAL_END_DATE,
    listOf(TEST_PLACE_ID),
    UserSimpleResponse(user, TEST_FILE_AUTHENTICATED_URL, false),
    TravelJournalVisibility.PUBLIC,
    listOf(createTravelCompanionSimpleResponse(travelCompanion)),
    true,
)

fun createTravelCompanionSimpleResponse(travelCompanion: TravelCompanion): TravelCompanionSimpleResponse =
    TravelCompanionSimpleResponse(
        travelCompanion.user?.username,
        TEST_FILE_AUTHENTICATED_URL,
    )

fun createTravelJournalResponse(travelJournal: TravelJournal = createTravelJournal()): TravelJournalResponse =
    TravelJournalResponse(
        travelJournal.id,
        travelJournal.title,
        travelJournal.travelStartDate,
        travelJournal.travelEndDate,
        travelJournal.visibility,
        TEST_IS_BOOK_MARKED_FALSE,
        UserSimpleResponse(createUser(), TEST_FILE_AUTHENTICATED_URL, false),
        listOf(createTravelJournalContentResponse()),
        listOf(createTravelCompanionResponse()),
    )

fun createTravelJournalContentResponse(travelJournalContent: TravelJournalContent = createTravelJournalContent()): TravelJournalContentResponse =
    TravelJournalContentResponse(
        travelJournalContent,
        TEST_TRAVEL_JOURNAL_LATITUDES to TEST_TRAVEL_JOURNAL_LONGITUDES,
        listOf(createTravelJournalContentImageResponse()),
    )

fun createTravelCompanionResponse(): TravelCompanionResponse =
    TravelCompanionResponse.fromRegisteredUser(createOtherUser(), TEST_GENERATED_FILE_NAME, false)

fun createTravelJournalContentImageResponse(travelJournalContentImageId: TravelJournalContentImage = createTravelJournalContentImage()): TravelJournalContentImageResponse =
    TravelJournalContentImageResponse(
        travelJournalContentImageId.id,
        travelJournalContentImageId.contentImage.name,
        TEST_GENERATED_FILE_NAME,
    )

fun createTaggedTravelJournalResponse(): TaggedTravelJournalResponse =
    TaggedTravelJournalResponse(
        TEST_TRAVEL_JOURNAL_ID,
        TEST_TRAVEL_JOURNAL_TITLE,
        TEST_FILE_AUTHENTICATED_URL,
        UserSimpleResponse(createUser(), TEST_FILE_AUTHENTICATED_URL, true),
        TEST_TRAVEL_JOURNAL_START_DATE,
    )

fun createSliceTaggedTravelJournalResponse(): SliceResponse<TaggedTravelJournalResponse> =
    SliceResponse(
        size = TEST_DEFAULT_SIZE,
        content = listOf(createTaggedTravelJournalResponse()),
    )

fun createTravelJournalSimpleResponse(
    id: Long = TEST_TRAVEL_JOURNAL_ID,
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    mainImageUrl: String = TEST_FILE_AUTHENTICATED_URL,
) = TravelJournalSimpleResponse(
    travelJournalId = id,
    title = title,
    mainImageUrl = mainImageUrl,
)
