package kr.weit.odya.support

import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmark
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkSortType
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TravelJournalBookmarkSummaryResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import java.time.LocalDate

private const val TEST_TRAVEL_JOURNAL_BOOKMARK_ID = 1L
private const val TEST_OTHER_TRAVEL_JOURNAL_BOOKMARK_ID = 2L
const val TEST_IS_BOOK_MARKED_FALSE = false
val TEST_TRAVEL_JOURNAL_BOOKMARK_SORT_TYPE = TravelJournalBookmarkSortType.LATEST

fun createTravelJournalBookmark(
    id: Long = TEST_TRAVEL_JOURNAL_BOOKMARK_ID,
    user: User = createUser(),
    travelJournal: TravelJournal = createTravelJournal(),
): TravelJournalBookmark = TravelJournalBookmark(
    id = id,
    travelJournal = travelJournal,
    user = user,
)

fun createOtherTravelJournalBookmark(
    id: Long = TEST_OTHER_TRAVEL_JOURNAL_BOOKMARK_ID,
    user: User = createUser(),
    travelJournal: TravelJournal = createTravelJournal(),
): TravelJournalBookmark = TravelJournalBookmark(
    id = id,
    travelJournal = travelJournal,
    user = user,
)

fun createTravelJournalBookmark(
    user: User = createUser(),
    travelJournal: TravelJournal = createTravelJournal(),
): TravelJournalBookmark = TravelJournalBookmark(
    travelJournal = travelJournal,
    user = user,
)

fun createSliceTravelJournalBookmarkSummaryResponse(
    hasNext: Boolean = false,
    travelJournalBookmarkSummaryResponses: List<TravelJournalBookmarkSummaryResponse> = listOf(
        createTravelJournalBookmarkSummaryResponse(),
        createTravelJournalBookmarkSummaryResponse(
            travelJournalId = TEST_OTHER_TRAVEL_JOURNAL_ID,
            title = TEST_OTHER_TRAVEL_JOURNAL_TITLE,
            userSimpleResponse = createOtherSimpleUserResponse(),
        ),
    ),
): SliceResponse<TravelJournalBookmarkSummaryResponse> = SliceResponse(hasNext, travelJournalBookmarkSummaryResponses)

fun createTravelJournalBookmarkSummaryResponse(
    travelJournalBookmarkId: Long = TEST_OTHER_TRAVEL_JOURNAL_BOOKMARK_ID,
    travelJournalId: Long = TEST_TRAVEL_JOURNAL_ID,
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    travelStartDate: LocalDate = TEST_TRAVEL_JOURNAL_START_DATE,
    travelJournalMainImageUrl: String = TEST_GENERATED_FILE_NAME,
    userSimpleResponse: UserSimpleResponse = createSimpleUserResponse(),
): TravelJournalBookmarkSummaryResponse = TravelJournalBookmarkSummaryResponse(
    travelJournalBookmarkId = travelJournalBookmarkId,
    travelJournalId = travelJournalId,
    title = title,
    travelStartDate = travelStartDate,
    travelJournalMainImageUrl = travelJournalMainImageUrl,
    writer = userSimpleResponse,
)
