package kr.weit.odya.support

import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalSortType
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournalbookmark.RepresentativeTravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.RepTravelJournalSummaryResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TravelCompanionSimpleResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import java.time.LocalDate

const val TEST_REP_TRAVEL_JOURNAL_ID = 1L
const val TEST_OTHER_REP_TRAVEL_JOURNAL_ID = 2L
val TEST_REP_TRAVEL_JOURNAL_SORT_TYPE = RepresentativeTravelJournalSortType.LATEST

fun createRepTravelJournal(
    id: Long = TEST_REP_TRAVEL_JOURNAL_ID,
    user: User = createUser(),
    travelJournal: TravelJournal = createTravelJournal(),
): RepresentativeTravelJournal = RepresentativeTravelJournal(
    id = id,
    travelJournal = travelJournal,
    user = user,
)

fun createSliceRepTravelJournalSummaryResponse(
    hasNext: Boolean = false,
    repTravelJournalSummaryResponses: List<RepTravelJournalSummaryResponse> = listOf(
        createRepTravelJournalSummaryResponse(),
    ),
): SliceResponse<RepTravelJournalSummaryResponse> = SliceResponse(hasNext, repTravelJournalSummaryResponses)

fun createRepTravelJournalSummaryResponse(
    repTravelJournalId: Long = TEST_REP_TRAVEL_JOURNAL_ID,
    travelJournalId: Long = TEST_TRAVEL_JOURNAL_ID,
    title: String = TEST_TRAVEL_JOURNAL_TITLE,
    travelStartDate: LocalDate = TEST_TRAVEL_JOURNAL_START_DATE,
    travelEndDate: LocalDate = TEST_TRAVEL_JOURNAL_END_DATE,
    travelJournalMainImageUrl: String = TEST_GENERATED_FILE_NAME,
    userSimpleResponse: UserSimpleResponse = createSimpleUserResponse(),
    travelCompanionSimpleResponses: List<TravelCompanionSimpleResponse>? = listOf(
        createTravelCompanionSimpleResponse(createTravelCompanionById()),
        createTravelCompanionSimpleResponse(createTravelCompanionByName()),
    ),
): RepTravelJournalSummaryResponse = RepTravelJournalSummaryResponse(
    repTravelJournalId = repTravelJournalId,
    travelJournalId = travelJournalId,
    title = title,
    travelStartDate = travelStartDate,
    travelEndDate = travelEndDate,
    travelJournalMainImageUrl = travelJournalMainImageUrl,
    writer = userSimpleResponse,
    travelCompanionSimpleResponses = travelCompanionSimpleResponses,
)
