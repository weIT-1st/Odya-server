package kr.weit.odya.service.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import kr.weit.odya.domain.traveljournal.Coordinates
import kr.weit.odya.domain.traveljournal.TravelCompanion
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentInformation
import kr.weit.odya.domain.traveljournal.TravelJournalInformation
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
        user = user,
        travelJournalInformation = TravelJournalInformation(
            title = title,
            travelStartDate = travelStartDate,
            travelEndDate = travelEndDate,
            visibility = visibility,
        ),
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
    fun toTravelJournalContentInformation() = TravelJournalContentInformation(
        content = content,
        placeId = placeId,
        coordinates = Coordinates.of(latitudes, longitudes),
        travelDate = travelDate,
    )
}

data class TravelJournalResponse(
    val travelJournalId: Long,
    val title: String,
    val travelStartDate: LocalDate,
    val travelEndDate: LocalDate,
    val visibility: TravelJournalVisibility,
    val isBookmarked: Boolean,
    val writer: UserSimpleResponse,
    val travelJournalContents: List<TravelJournalContentResponse>,
    val travelJournalCompanions: List<TravelCompanionResponse>,
) {
    constructor(
        travelJournal: TravelJournal,
        isBookmarked: Boolean,
        writerProfileUrl: String,
        travelJournalContents: List<TravelJournalContentResponse>,
        travelJournalCompanions: List<TravelCompanionResponse>,
    ) : this(
        travelJournal.id,
        travelJournal.title,
        travelJournal.travelStartDate,
        travelJournal.travelEndDate,
        travelJournal.visibility,
        isBookmarked,
        UserSimpleResponse(travelJournal.user, writerProfileUrl),
        travelJournalContents,
        travelJournalCompanions,
    )
}

data class TravelJournalContentResponse(
    val travelJournalContentId: Long,
    val content: String?,
    val placeId: String?,
    val latitudes: List<Double>?,
    val longitudes: List<Double>?,
    val travelDate: LocalDate,
    val travelJournalContentImages: List<TravelJournalContentImageResponse>,
) {
    constructor(
        travelJournalContent: TravelJournalContent,
        coordinatePair: Pair<List<Double>, List<Double>>?,
        travelJournalContentImages: List<TravelJournalContentImageResponse>,
    ) : this(
        travelJournalContent.id,
        travelJournalContent.content,
        travelJournalContent.placeId,
        coordinatePair?.first ?: emptyList(),
        coordinatePair?.second ?: emptyList(),
        travelJournalContent.travelDate,
        travelJournalContentImages,
    )
}

data class TravelJournalContentImageResponse(
    val travelJournalContentImageId: Long,
    val contentImageName: String,
    val contentImageUrl: String,
)

data class TravelCompanionResponse private constructor(
    val userId: Long?,
    val nickname: String?,
    val profileUrl: String?,
    val isRegistered: Boolean,
) {
    companion object {
        fun fromRegisteredUser(user: User, profileUrl: String): TravelCompanionResponse {
            return TravelCompanionResponse(
                user.id,
                user.nickname,
                profileUrl,
                true,
            )
        }

        fun fromNonRegisteredUser(travelCompanion: TravelCompanion): TravelCompanionResponse {
            return TravelCompanionResponse(
                null,
                travelCompanion.username!!,
                null,
                false,
            )
        }
    }
}

data class TravelJournalUpdateRequest(
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
) {
    val travelDurationDays =
        Duration.between(travelStartDate.atStartOfDay(), travelEndDate.atStartOfDay()).toDaysPart().toInt() + 1
    val updateTravelCompanionTotalCount = (travelCompanionIds?.size ?: 0) + (travelCompanionNames?.size ?: 0)

    fun toTravelJournalInformation(): TravelJournalInformation {
        return TravelJournalInformation(
            title = title,
            travelStartDate = travelStartDate,
            travelEndDate = travelEndDate,
            visibility = visibility,
        )
    }
}

data class TravelJournalContentUpdateRequest(
    @field:Length(max = 200, message = "여행 일지 콘텐츠 내용은 최대 200자까지 입력 가능합니다.")
    val content: String?,
    @field:NullOrNotBlank(message = "여행 일지 콘텐츠의 장소는 필수 입력값입니다.")
    val placeId: String?,
    val latitudes: List<Double>?,
    val longitudes: List<Double>?,
    @field:PastOrPresent
    val travelDate: LocalDate,
    @field:Size(max = 15, message = "추가 이미지는 최대 15개까지 등록 가능합니다.")
    val updateContentImageNames: List<String>?,
    @field:Size(max = 15, message = "삭제 이미지는 최대 15개까지 등록 가능합니다.")
    val deleteContentImageIds: List<Long>?,
) {
    val updateImageTotalCount: Int = (updateContentImageNames?.size ?: 0) - (deleteContentImageIds?.size ?: 0)

    fun toTravelJournalContentInformation() = TravelJournalContentInformation(
        content = content,
        placeId = placeId,
        coordinates = Coordinates.of(latitudes, longitudes),
        travelDate = travelDate,
    )
}

data class TravelJournalSummaryResponse(
    val travelJournalId: Long,
    val title: String,
    val content: String?,
    val contentImageUrl: String,
    val travelStartDate: LocalDate,
    val travelEndDate: LocalDate,
    val placeIds: List<String>,
    val writer: UserSimpleResponse,
    val travelCompanionSimpleResponses: List<TravelCompanionSimpleResponse>?,
) {
    constructor(
        travelJournal: TravelJournal,
        content: String?,
        contentImageUrl: String,
        writerProfileUrl: String,
        travelCompanionSimpleResponses: List<TravelCompanionSimpleResponse>?,
    ) : this(
        travelJournal.id,
        travelJournal.title,
        content,
        contentImageUrl,
        travelJournal.travelStartDate,
        travelJournal.travelEndDate,
        travelJournal.travelJournalContents.mapNotNull { it.placeId },
        UserSimpleResponse(travelJournal.user, writerProfileUrl),
        travelCompanionSimpleResponses,
    )
}

data class TravelJournalSimpleResponse(
    val travelJournalId: Long,
    val title: String,
    val mainImageUrl: String?,
)

data class TaggedTravelJournalResponse(
    val travelJournalId: Long,
    val title: String,
    val mainImageUrl: String,
    val writer: UserSimpleResponse,
    @JsonFormat(pattern = "yyyy.MM.dd")
    val travelStartDate: LocalDate,
)

data class TravelCompanionSimpleResponse(
    val username: String?,
    val profileUrl: String?,
)
