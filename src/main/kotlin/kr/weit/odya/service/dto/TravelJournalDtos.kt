package kr.weit.odya.service.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import kr.weit.odya.domain.traveljournal.Coordinates
import kr.weit.odya.domain.traveljournal.TravelCompanion
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentImage
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.validator.NullOrNotBlank
import org.hibernate.validator.constraints.Length
import java.time.Duration
import java.time.LocalDate

data class TravelJournalRequest(
    @field:Length(min = 1, max = 20, message = "여행 일지 제목은 최소 1자, 최대 20자까지 입력 가능합니다.")
    val title: String,
    @field:PastOrPresent
    val travelStartDate: LocalDate,
    @field:PastOrPresent
    val travelEndDate: LocalDate,
    @field:NotNull(message = "여행 일지 공개 여부는 필수 입력값입니다.")
    val visibility: TravelJournalVisibility = TravelJournalVisibility.PUBLIC,
    val travelCompanionIds: List<Long>?,
    val travelCompanionNames: List<String>?,
    @field:Valid
    @field:Size(max = 15, message = "여행 일지 콘텐츠는 최대 15개까지 등록 가능합니다.")
    val travelJournalContentRequests: List<TravelJournalContentRequest>,
) {
    val travelDurationDays =
        Duration.between(travelStartDate.atStartOfDay(), travelEndDate.atStartOfDay()).toDaysPart().toInt() + 1
    val contentImageNameTotalCount: Int = travelJournalContentRequests.sumOf { it.contentImageNames.size }

    fun toEntity(
        user: User,
        travelCompanions: List<TravelCompanion>,
        travelJournalContents: List<TravelJournalContent>,
    ): TravelJournal = TravelJournal(
        title = title,
        travelStartDate = travelStartDate,
        travelEndDate = travelEndDate,
        visibility = visibility,
        user = user,
        travelCompanions = travelCompanions,
        travelJournalContents = travelJournalContents,
    )
}

data class TravelJournalContentRequest(
    @field:Length(max = 200, message = "여행 일지 콘텐츠 내용은 최대 200자까지 입력 가능합니다.")
    val content: String?,
    @field:NullOrNotBlank(message = "여행 일지 콘텐츠의 장소는 필수 입력값입니다.")
    val placeId: String?,
    val latitudes: List<Double>?,
    val longitudes: List<Double>?,
    @field:PastOrPresent
    val travelDate: LocalDate,
    @field:Size(min = 1, max = 15, message = "여행 일지 콘텐츠 이미지는 최소 1개, 최대 15개까지 등록 가능합니다.")
    val contentImageNames: List<String>,
) {
    fun toEntity(travelJournalContentImages: List<TravelJournalContentImage>) = TravelJournalContent(
        content = content,
        placeId = placeId,
        coordinates = if (latitudes != null && longitudes != null && latitudes.isNotEmpty()) {
            Coordinates(
                latitudes,
                longitudes,
            )
        } else {
            null
        },
        travelDate = travelDate,
        travelJournalContentImages = travelJournalContentImages,
    )
}
