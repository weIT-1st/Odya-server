package kr.weit.odya.support

import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.traveljournal.Coordinates
import kr.weit.odya.domain.traveljournal.TravelCompanion
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentImage
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.TravelJournalContentRequest
import kr.weit.odya.service.dto.TravelJournalRequest
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream
import java.time.LocalDate

const val TEST_TRAVEL_JOURNAL_TITLE = "testTitle"
const val TEST_TRAVEL_JOURNAL_CONTENT = "testContent"
const val TEST_OTHER_TRAVEL_JOURNAL_CONTENT = "testContent2"
const val TEST_TRAVEL_COMPANION_NAME = "testCompanion"
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
val TEST_TRAVEL_CONTENT_IMAGE_MAP = createImageMap()
val TEST_TRAVEL_JOURNAL = createTravelJournal()

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
    content: String = TEST_TRAVEL_JOURNAL_CONTENT,
    latitudes: List<Double> = TEST_TRAVEL_JOURNAL_LATITUDES,
    longitudes: List<Double> = TEST_TRAVEL_JOURNAL_LONGITUDES,
    placeId: String = TEST_PLACE_ID,
    travelDate: LocalDate = TEST_TRAVEL_DATE,
    contentImageNames: List<String>? = listOf(TEST_IMAGE_FILE_WEBP),
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

fun createImageMap(fileName: String = TEST_IMAGE_FILE_WEBP, otherFileName: String = TEST_OTHER_IMAGE_FILE_WEBP) = mapOf(
    fileName to createMockImageFile(),
    otherFileName to createMockOtherImageFile(),
)

fun createImageNamePairs() = listOf(
    TEST_GENERATED_FILE_NAME to TEST_IMAGE_FILE_WEBP,
    TEST_GENERATED_FILE_NAME to TEST_OTHER_IMAGE_FILE_WEBP,
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
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    travelStartDate: LocalDate = TEST_TRAVEL_JOURNAL_START_DATE,
    travelEndDate: LocalDate = TEST_TRAVEL_JOURNAL_END_DATE,
    visibility: TravelJournalVisibility = TravelJournalVisibility.PUBLIC,
    user: User = createUser(),
    travelCompanions: List<TravelCompanion> = TEST_TRAVEL_COMPANIONS,
    travelJournalContents: List<TravelJournalContent> = listOf(
        createTravelJournalContent(),
        createTravelJournalContent(
            travelJournalContentImages = listOf(createTravelJournalContentImage(createOtherContentImage())),
        ),
    ),
) = TravelJournal(
    title = title,
    travelStartDate = travelStartDate,
    travelEndDate = travelEndDate,
    visibility = visibility,
    user = user,
    travelCompanions = travelCompanions,
    travelJournalContents = travelJournalContents,
)

fun createTravelJournalContent(
    content: String = TEST_TRAVEL_JOURNAL_CONTENT,
    placeId: String = TEST_PLACE_ID,
    latitudes: List<Double> = TEST_TRAVEL_JOURNAL_LATITUDES,
    longitudes: List<Double> = TEST_TRAVEL_JOURNAL_LONGITUDES,
    travelDate: LocalDate = TEST_TRAVEL_DATE,
    travelJournalContentImages: List<TravelJournalContentImage>? = listOf(
        createTravelJournalContentImage(),
    ),
) = TravelJournalContent(
    content = content,
    placeId = placeId,
    coordinates = Coordinates(latitudes, longitudes),
    travelDate = travelDate,
    travelJournalContentImages = travelJournalContentImages ?: emptyList(),
)

fun createTravelJournalContentImage(contentImage: ContentImage = createContentImage()) = TravelJournalContentImage(
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
