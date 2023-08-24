package kr.weit.odya.service.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
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
    val contentImageNameTotalCount: Int = travelJournalContentRequests.sumOf { it.contentImageNames?.size ?: 0 }

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
    @field:Length(min = 1, max = 200, message = "여행 일지 콘텐츠 내용은 최소 1자, 최대 200자까지 입력 가능합니다.")
    val content: String,
    @field:NotBlank(message = "여행 일지 콘텐츠의 장소는 필수 입력값입니다.")
    val placeId: String,
    @field:NotEmpty(message = "여행 일지 콘텐츠의 위도(x)는 최소 1개 이상 입력해야 합니다.")
    val latitudes: List<Double>,
    @field:NotEmpty(message = "여행 일지 콘텐츠의 경도(y)는 최소 1개 이상 입력해야 합니다.")
    val longitudes: List<Double>,
    @field:PastOrPresent
    val travelDate: LocalDate,
    @field:Size(max = 15, message = "여행 일지 콘텐츠 이미지는 최대 15개까지 등록 가능합니다.")
    val contentImageNames: List<String>?,
) {
    fun toEntity(travelJournalContentImages: List<TravelJournalContentImage>?) = TravelJournalContent(
        content = content,
        placeId = placeId,
        coordinates = Coordinates(latitudes, longitudes),
        travelDate = travelDate,
        travelJournalContentImages = travelJournalContentImages ?: emptyList(),
    )
}
