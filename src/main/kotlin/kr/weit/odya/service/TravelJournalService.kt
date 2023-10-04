package kr.weit.odya.service

import com.google.maps.model.PlaceDetails
import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.client.GoogleMapsClient
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowerFcmTokens
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.report.deleteAllByUserId
import kr.weit.odya.domain.traveljournal.TravelCompanion
import kr.weit.odya.domain.traveljournal.TravelCompanionRepository
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentImage
import kr.weit.odya.domain.traveljournal.TravelJournalContentInformation
import kr.weit.odya.domain.traveljournal.TravelJournalContentUpdateEvent
import kr.weit.odya.domain.traveljournal.TravelJournalDeleteEvent
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalSortType
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.traveljournal.findTravelCompanionId
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.traveljournal.getFriendTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getMyTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getRecommendTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getTaggedTravelJournalSliceBy
import kr.weit.odya.domain.traveljournal.getTravelJournalSliceBy
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TaggedTravelJournalResponse
import kr.weit.odya.service.dto.TravelCompanionResponse
import kr.weit.odya.service.dto.TravelCompanionSimpleResponse
import kr.weit.odya.service.dto.TravelJournalContentImageResponse
import kr.weit.odya.service.dto.TravelJournalContentResponse
import kr.weit.odya.service.dto.TravelJournalContentUpdateRequest
import kr.weit.odya.service.dto.TravelJournalRequest
import kr.weit.odya.service.dto.TravelJournalResponse
import kr.weit.odya.service.dto.TravelJournalSummaryResponse
import kr.weit.odya.service.dto.TravelJournalUpdateRequest
import kr.weit.odya.service.dto.UserSimpleResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

private const val MAX_TRAVEL_DAYS = 15
private const val MAX_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT = 15
private const val MAX_TRAVEL_COMPANION_COUNT = 10

@Service
class TravelJournalService(
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val followRepository: FollowRepository,
    private val communityRepository: CommunityRepository,
    private val fileService: FileService,
    private val eventPublisher: ApplicationEventPublisher,
    private val reportTravelJournalRepository: ReportTravelJournalRepository,
    private val contentImageRepository: ContentImageRepository,
    private val travelCompanionRepository: TravelCompanionRepository,
    private val googleMapsClient: GoogleMapsClient,
    private val travelJournalBookmarkRepository: TravelJournalBookmarkRepository,
) {
    @Transactional
    fun createTravelJournal(
        userId: Long,
        travelJournalRequest: TravelJournalRequest,
        imageNamePairs: List<Pair<String, String>>,
        placeDetailsMap: Map<String, PlaceDetails>,
    ): Long {
        val register = userRepository.getByUserId(userId)
        val travelCompanions =
            getTravelCompanions(travelJournalRequest.travelCompanionIds, travelJournalRequest.travelCompanionNames)
        val contentImageMap = getContentImageMap(register, imageNamePairs)
        val travelJournalContents =
            travelJournalRequest.travelJournalContentRequests.map { travelJournalContentRequest ->
                val contentImages =
                    getContentImages(
                        travelJournalContentRequest.contentImageNames,
                        contentImageMap,
                        placeDetailsMap,
                        travelJournalContentRequest.placeId,
                    )
                createTravelJournalContent(
                    contentImages,
                    travelJournalContentRequest.toTravelJournalContentInformation(),
                )
            }
        val travelJournal = travelJournalRequest.toEntity(register, travelCompanions, travelJournalContents)
        val savedTravelJournal = travelJournalRepository.save(travelJournal)
        if (travelJournal.visibility != TravelJournalVisibility.PRIVATE) {
            publishTravelJournalPushEvent(register, travelJournal)
        }
        return savedTravelJournal.id
    }

    fun uploadTravelContentImages(
        contentImageNames: List<String>,
        imageMap: Map<String, MultipartFile>?,
    ): List<Pair<String, String>> = contentImageNames.mapNotNull { contentImageName ->
        imageMap?.getValue(contentImageName)?.let { image ->
            val fileName = fileService.saveFile(image)
            fileName to image.originalFilename!!
        }
    }

    fun validateTravelJournalRequest(
        travelJournalRequest: TravelJournalRequest,
        imageMap: Map<String, MultipartFile>?,
    ) {
        require(travelJournalRequest.contentImageNameTotalCount == (imageMap?.size ?: 0)) {
            "여행 일지 콘텐츠의 이미지 이름 총 개수(${travelJournalRequest.contentImageNameTotalCount})는 여행 일지의 이미지 개수(${imageMap?.size ?: 0})와 같아야 합니다."
        }
        travelJournalRequest.travelJournalContentRequests.forEach {
            it.contentImageNames.forEach { contentImageName ->
                require(imageMap?.containsKey(contentImageName) ?: false) {
                    "여행 일지 콘텐츠의 이미지 이름($contentImageName)은 여행 이미지 파일 이름과 일치해야 합니다."
                }
            }
        }
        require(travelJournalRequest.travelCompanionIds.orEmpty().size + travelJournalRequest.travelCompanionNames.orEmpty().size <= MAX_TRAVEL_COMPANION_COUNT) {
            "여행 일지 친구는 최대 ${MAX_TRAVEL_COMPANION_COUNT}명까지 등록 가능합니다."
        }
        travelJournalRequest.travelCompanionIds?.let {
            if (!userRepository.existsAllByUserIds(it, it.size)) {
                throw NoSuchElementException("여행 일지의 친구 아이디는 존재하는 사용자 아이디여야 합니다.")
            }
        }
        require(
            travelJournalRequest.travelStartDate == travelJournalRequest.travelEndDate || travelJournalRequest.travelStartDate.isBefore(
                travelJournalRequest.travelEndDate,
            ),
        ) {
            "여행 일지의 시작일(${travelJournalRequest.travelStartDate})은 종료일(${travelJournalRequest.travelEndDate})보다 이전이거나 같아야 합니다."
        }
        require(travelJournalRequest.travelDurationDays <= MAX_TRAVEL_DAYS) {
            "여행 일지의 여행 기간(${travelJournalRequest.travelDurationDays})은 ${MAX_TRAVEL_DAYS}일 이하이어야 합니다."
        }
        require(travelJournalRequest.travelDurationDays >= travelJournalRequest.travelJournalContentRequests.size) {
            "여행 일지의 여행 기간(${travelJournalRequest.travelDurationDays})은 여행 일지 콘텐츠의 개수(${travelJournalRequest.travelJournalContentRequests.size})보다 크거나 같아야 합니다."
        }
        travelJournalRequest.travelJournalContentRequests.forEach {
            require(it.travelDate in (travelJournalRequest.travelStartDate..travelJournalRequest.travelEndDate)) {
                "여행 일지 콘텐츠의 여행 일자(${it.travelDate})는 여행 일지의 시작일(${travelJournalRequest.travelStartDate})과 종료일(${travelJournalRequest.travelEndDate}) 사이여야 합니다."
            }
            require(it.latitudes?.size == it.longitudes?.size) {
                "여행 일지 콘텐츠의 위도 개수(${it.latitudes?.size})와 경도 개수(${it.longitudes?.size})는 같아야 합니다."
            }
        }
    }

    fun getImageMap(images: List<MultipartFile>?): Map<String, MultipartFile>? =
        images?.associateBy {
            require(!it.originalFilename.isNullOrEmpty()) { IllegalArgumentException("파일 원본 이름은 필수 값입니다.") }
            it.originalFilename!!
        }

    @Transactional(readOnly = true)
    fun getTravelJournal(travelJournalId: Long, userId: Long): TravelJournalResponse {
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        validateUserReadPermission(travelJournal, userId)
        return getTravelJournalResponse(userId, travelJournal)
    }

    @Transactional(readOnly = true)
    fun getTravelJournals(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): SliceResponse<TravelJournalSummaryResponse> {
        val travelJournals = travelJournalRepository.getTravelJournalSliceBy(userId, size, lastId, sortType)
        return SliceResponse(
            size,
            getTravelJournalSimpleResponses(travelJournals),
        )
    }

    @Transactional(readOnly = true)
    fun getMyTravelJournals(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): SliceResponse<TravelJournalSummaryResponse> {
        val travelJournals = travelJournalRepository.getMyTravelJournalSliceBy(userId, size, lastId, sortType)
        return SliceResponse(
            size,
            getTravelJournalSimpleResponses(travelJournals),
        )
    }

    @Transactional(readOnly = true)
    fun getFriendTravelJournals(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): SliceResponse<TravelJournalSummaryResponse> {
        val travelJournals = travelJournalRepository.getFriendTravelJournalSliceBy(userId, size, lastId, sortType)
        return SliceResponse(
            size,
            getTravelJournalSimpleResponses(travelJournals),
        )
    }

    @Transactional(readOnly = true)
    fun getRecommendTravelJournals(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalSortType,
    ): SliceResponse<TravelJournalSummaryResponse> {
        val user = userRepository.getByUserId(userId)
        val travelJournals = travelJournalRepository.getRecommendTravelJournalSliceBy(user, size, lastId, sortType)
        return SliceResponse(
            size,
            getTravelJournalSimpleResponses(travelJournals),
        )
    }

    @Transactional(readOnly = true)
    fun getTaggedTravelJournals(userId: Long, size: Int, lastId: Long?): SliceResponse<TaggedTravelJournalResponse> {
        val user = userRepository.getByUserId(userId)
        val travelJournals = travelJournalRepository.getTaggedTravelJournalSliceBy(user, size, lastId)
        return SliceResponse(
            size,
            getTaggedTravelJournalResponses(travelJournals),
        )
    }

    @Transactional
    fun updateTravelJournal(
        travelJournalId: Long,
        userId: Long,
        travelJournalUpdateRequest: TravelJournalUpdateRequest,
    ) {
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        validateTravelJournalUpdateRequest(travelJournal, userId, travelJournalUpdateRequest)
        travelJournal.changeTravelJournalInformation(travelJournalUpdateRequest.toTravelJournalInformation())
        updateTravelCompanions(travelJournal, travelJournalUpdateRequest)
    }

    @Transactional
    fun updateTravelJournalContent(
        travelJournalId: Long,
        travelJournalContentId: Long,
        userId: Long,
        travelJournalContentUpdateRequest: TravelJournalContentUpdateRequest,
        imageNamePairs: List<Pair<String, String>>,
        placeDetailsMap: Map<String, PlaceDetails>,
    ) {
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        val travelJournalContent = getTravelJournalContent(travelJournal, travelJournalContentId)

        // 내용 변경
        travelJournalContent.changeTravelJournalContent(travelJournalContentUpdateRequest.toTravelJournalContentInformation())

        // 이미지 추가
        val register = userRepository.getByUserId(userId)
        val contentImageMap = getContentImageMap(register, imageNamePairs)
        val newTravelJournalContentImages = getContentImages(
            travelJournalContentUpdateRequest.updateContentImageNames ?: emptyList(),
            contentImageMap,
            placeDetailsMap,
            travelJournalContentUpdateRequest.placeId,
        ).map { TravelJournalContentImage(contentImage = it) }
        travelJournalContent.addTravelJournalContentImages(newTravelJournalContentImages)

        // 이미지 삭제
        val deleteTravelJournalContentImages = travelJournalContent.travelJournalContentImages.filter {
            travelJournalContentUpdateRequest.deleteContentImageIds?.contains(it.id) == true
        }
        travelJournalContent.deleteTravelJournalContentImages(deleteTravelJournalContentImages)

        // 이미지 Object Storage 삭제
        eventPublisher.publishEvent(TravelJournalContentUpdateEvent(deleteTravelJournalContentImages.map { it.contentImage.name }))
    }

    @Transactional(readOnly = true)
    fun validateTravelJournalContentUpdateRequest(
        travelJournalId: Long,
        travelJournalContentId: Long,
        userId: Long,
        imageMap: Map<String, MultipartFile>?,
        travelJournalContentUpdateRequest: TravelJournalContentUpdateRequest,
    ) {
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        val travelJournalContent = getTravelJournalContent(travelJournal, travelJournalContentId)
        validateUserPermission(travelJournal, userId)
        require(travelJournalContent.travelJournalContentImages.size + travelJournalContentUpdateRequest.updateImageTotalCount <= MAX_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT) {
            "여행 일지 콘텐츠의 이미지 개수(${travelJournalContent.travelJournalContentImages.size})와 추가 이미지 개수(${travelJournalContentUpdateRequest.updateImageTotalCount})의 합은 최대 $MAX_TRAVEL_JOURNAL_CONTENT_IMAGE_COUNT 개까지 등록 가능합니다."
        }
        travelJournalContentUpdateRequest.updateContentImageNames?.forEach {
            require(imageMap?.containsKey(it) ?: false) {
                "추가할 여행 일지 콘텐츠의 이미지 이름($it)은 여행 이미지 파일 이름과 일치해야 합니다."
            }
        }
        require(travelJournalContentUpdateRequest.travelDate in (travelJournal.travelStartDate..travelJournal.travelEndDate)) {
            "여행 일지 콘텐츠의 여행 일자(${travelJournalContentUpdateRequest.travelDate})는 여행 일지의 시작일(${travelJournal.travelStartDate})과 종료일(${travelJournal.travelEndDate}) 사이여야 합니다."
        }
        require(travelJournalContentUpdateRequest.latitudes?.size == travelJournalContentUpdateRequest.longitudes?.size) {
            "여행 일지 콘텐츠의 위도 개수(${travelJournalContentUpdateRequest.latitudes?.size})와 경도 개수(${travelJournalContentUpdateRequest.longitudes?.size})는 같아야 합니다."
        }
    }

    @Transactional
    fun deleteTravelJournal(travelJournalId: Long, userId: Long) {
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        validateUserPermission(travelJournal, userId)
        val travelJournalContentImageNames = getTravelJournalContentImageNames(travelJournal)
        // Community - TravelJournal FK 위반으로 인한 null 처리
        communityRepository.updateTravelJournalIdToNull(travelJournalId)
        reportTravelJournalRepository.deleteAllByTravelJournalId(travelJournalId)
        travelJournalRepository.delete(travelJournal)
        eventPublisher.publishEvent(TravelJournalDeleteEvent(travelJournalContentImageNames))
    }

    @Transactional
    fun deleteTravelJournalContent(travelJournalId: Long, travelJournalContentId: Long, userId: Long) {
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        validateUserPermission(travelJournal, userId)
        val travelJournalContent = getTravelJournalContent(travelJournal, travelJournalContentId)
        val deleteTravelJournalContentImageNames =
            travelJournalContent.travelJournalContentImages.map { it.contentImage.name }
        travelJournal.deleteTravelJournalContent(travelJournalContent)
        eventPublisher.publishEvent(TravelJournalDeleteEvent(deleteTravelJournalContentImageNames))
    }

    @Transactional
    fun deleteTravelJournalByUserId(userId: Long) {
        reportTravelJournalRepository.deleteAllByUserId(userId)
        travelCompanionRepository.deleteAllByUserId(userId)
        travelJournalRepository.deleteAllByUserId(userId)
        contentImageRepository.deleteAllByUserId(userId)
        eventPublisher.publishEvent(TravelJournalDeleteEvent(contentImageRepository.findAllByUserId(userId)))
    }

    @Transactional
    fun removeTravelCompanion(userId: Long, travelJournalId: Long) {
        val user = userRepository.getByUserId(userId)
        travelCompanionRepository.deleteById(getRemoveTravelCompanionId(user, travelJournalId))
    }

    private fun updateTravelCompanions(
        travelJournal: TravelJournal,
        travelJournalUpdateRequest: TravelJournalUpdateRequest,
    ) {
        val deleteTravelCompanionIds = travelJournal.travelCompanions
            .filter {
                !(travelJournalUpdateRequest.travelCompanionIds?.contains(it.user?.id) ?: false) || it.user == null
            }
            .map { it.id }
        travelCompanionRepository.deleteAllByIdInBatch(deleteTravelCompanionIds)

        val updateTravelCompanionIds = travelJournalUpdateRequest.travelCompanionIds.orEmpty().filter {
            travelJournal.travelCompanions.none { travelCompanion -> travelCompanion.user?.id == it }
        }
        val newTravelCompanions = getTravelCompanions(
            updateTravelCompanionIds,
            travelJournalUpdateRequest.travelCompanionNames,
        )
        travelJournal.addTravelCompanions(newTravelCompanions)
    }

    fun getPlaceDetailsMap(placeIdList: Set<String>): Map<String, PlaceDetails> =
        placeIdList.associateWith { googleMapsClient.findPlaceDetailsByPlaceId(it) }

    private fun getTravelJournalContent(
        travelJournal: TravelJournal,
        travelJournalContentId: Long,
    ) = travelJournal.travelJournalContents.firstOrNull {
        it.id == travelJournalContentId
    } ?: throw NoSuchElementException("해당 여행 일지 콘텐츠($travelJournalContentId)가 존재하지 않습니다.")

    private fun validateTravelJournalUpdateRequest(
        travelJournal: TravelJournal,
        userId: Long,
        travelJournalUpdateRequest: TravelJournalUpdateRequest,
    ) {
        validateUserPermission(travelJournal, userId)
        require(
            travelJournalUpdateRequest.travelStartDate == travelJournalUpdateRequest.travelEndDate || travelJournalUpdateRequest.travelStartDate.isBefore(
                travelJournalUpdateRequest.travelEndDate,
            ),
        ) {
            "여행 일지의 시작일(${travelJournalUpdateRequest.travelStartDate})은 종료일(${travelJournalUpdateRequest.travelEndDate})보다 이전이거나 같아야 합니다."
        }
        require(travelJournalUpdateRequest.travelDurationDays <= MAX_TRAVEL_DAYS) {
            "여행 일지의 여행 기간(${travelJournalUpdateRequest.travelDurationDays})은 ${MAX_TRAVEL_DAYS}일 이하이어야 합니다."
        }
        require(travelJournalUpdateRequest.travelDurationDays >= travelJournal.travelJournalContents.size) {
            "여행 일지의 여행 기간(${travelJournalUpdateRequest.travelDurationDays})은 여행 일지 콘텐츠의 개수(${travelJournal.travelJournalContents.size})보다 크거나 같아야 합니다."
        }
        travelJournal.travelJournalContents.forEach {
            require(it.travelDate in (travelJournalUpdateRequest.travelStartDate..travelJournalUpdateRequest.travelEndDate)) {
                "여행 일지 콘텐츠의 여행 일자(${it.travelDate})는 여행 일지의 시작일(${travelJournalUpdateRequest.travelStartDate})과 종료일(${travelJournalUpdateRequest.travelEndDate}) 사이여야 합니다."
            }
        }
        require(travelJournalUpdateRequest.updateTravelCompanionTotalCount <= MAX_TRAVEL_COMPANION_COUNT) {
            "여행 일지 친구는 최대 ${MAX_TRAVEL_COMPANION_COUNT}명까지 등록 가능합니다."
        }
    }

    private fun validateUserPermission(travelJournal: TravelJournal, userId: Long) {
        if (travelJournal.user.id != userId) {
            throw ForbiddenException("요청 사용자($userId)는 해당 요청을 처리할 권한이 없습니다.")
        }
    }

    private fun getTravelJournalSimpleResponses(travelJournals: List<TravelJournal>) =
        travelJournals.map {
            val companionSimpleResponses = getTravelCompanionSimpleResponses(it)
            TravelJournalSummaryResponse(
                it,
                it.travelJournalContents[0].content,
                fileService.getPreAuthenticatedObjectUrl(it.travelJournalContents[0].travelJournalContentImages[0].contentImage.name),
                fileService.getPreAuthenticatedObjectUrl(it.user.profile.profileName),
                companionSimpleResponses,
            )
        }

    private fun getTravelCompanionSimpleResponses(it: TravelJournal): List<TravelCompanionSimpleResponse> =
        it.travelCompanions
            .map { travelCompanion ->
                if (travelCompanion.user != null) {
                    TravelCompanionSimpleResponse(
                        travelCompanion.user!!.username,
                        fileService.getPreAuthenticatedObjectUrl(it.user.profile.profileName),
                    )
                } else {
                    TravelCompanionSimpleResponse(travelCompanion.username, null)
                }
            }

    private fun getTravelJournalResponse(userId: Long, travelJournal: TravelJournal): TravelJournalResponse {
        val travelJournalContentResponses = getTravelJournalContentResponses(travelJournal)
        val travelCompanionResponses = getTravelCompanionResponses(travelJournal)
        val isBookmarked =
            travelJournalBookmarkRepository.existsByUserIdAndTravelJournal(userId, travelJournal)
        return TravelJournalResponse(
            travelJournal,
            isBookmarked,
            fileService.getPreAuthenticatedObjectUrl(travelJournal.user.profile.profileName),
            travelJournalContentResponses,
            travelCompanionResponses,
        )
    }

    private fun getTravelCompanionResponses(travelJournal: TravelJournal): List<TravelCompanionResponse> =
        travelJournal.travelCompanions.map {
            if (it.user != null) {
                TravelCompanionResponse.fromRegisteredUser(
                    it.user!!,
                    fileService.getPreAuthenticatedObjectUrl(it.user!!.profile.profileName),
                )
            } else {
                TravelCompanionResponse.fromNonRegisteredUser(it)
            }
        }

    private fun getTravelJournalContentResponses(travelJournal: TravelJournal): List<TravelJournalContentResponse> {
        return travelJournal.travelJournalContents.map { travelJournalContent ->
            val travelJournalContentImageResponses =
                travelJournalContent.travelJournalContentImages.map { travelJournalContentImage ->
                    TravelJournalContentImageResponse(
                        travelJournalContentImage.id,
                        travelJournalContentImage.contentImage.name,
                        fileService.getPreAuthenticatedObjectUrl(travelJournalContentImage.contentImage.name),
                    )
                }
            TravelJournalContentResponse(
                travelJournalContent,
                travelJournalContent.coordinates?.splitCoordinates(),
                travelJournalContentImageResponses,
            )
        }
    }

    private fun getTaggedTravelJournalResponses(travelJournals: List<TravelJournal>): List<TaggedTravelJournalResponse> {
        return travelJournals.map { travelJournal ->
            val mainImageAuthenticatedUrl =
                fileService.getPreAuthenticatedObjectUrl(travelJournal.travelJournalContents[0].travelJournalContentImages[0].contentImage.name)
            TaggedTravelJournalResponse(
                travelJournal.id,
                travelJournal.title,
                mainImageAuthenticatedUrl,
                UserSimpleResponse(
                    travelJournal.user,
                    fileService.getPreAuthenticatedObjectUrl(travelJournal.user.profile.profileName),
                ),
                travelJournal.travelStartDate,
            )
        }
    }

    private fun validateUserReadPermission(travelJournal: TravelJournal, userId: Long) {
        if (travelJournal.visibility == TravelJournalVisibility.PRIVATE && travelJournal.user.id != userId) {
            throw ForbiddenException("비공개 여행 일지는 작성자만 조회할 수 있습니다.")
        }
        if (travelJournal.visibility == TravelJournalVisibility.FRIEND_ONLY && travelJournal.user.id != userId &&
            !(followRepository.existsByFollowerIdAndFollowingId(travelJournal.user.id, userId))
        ) {
            throw ForbiddenException("친구가 아닌 사용자는 친구에게만 공개하는 여행 일지를 조회할 수 없습니다.")
        }
    }

    private fun publishTravelJournalPushEvent(
        user: User,
        travelJournal: TravelJournal,
    ) {
        // 같이간 친구에게 알림
        eventPublisher.publishEvent(
            travelJournal.travelCompanions.mapNotNull { travelCompanion ->
                travelCompanion.user?.fcmToken
            }.let { fcmTokens ->
                PushNotificationEvent(
                    title = "같이간 친구 알림",
                    body = "${user.nickname}님이 여행 일지에 같이간 친구로 등록했어요!",
                    tokens = fcmTokens,
                    data = mapOf("travelJournalId" to travelJournal.id.toString()),
                )
            },
        )

        // 팔로워에게 알림
        eventPublisher.publishEvent(
            PushNotificationEvent(
                title = "여행일지 알림",
                body = "${user.nickname}님이 여행 일지를 작성했어요!",
                tokens = followRepository.getFollowerFcmTokens(user.id),
                data = mapOf("travelJournalId" to travelJournal.id.toString()),
            ),
        )
    }

    private fun createTravelJournalContent(
        contentImages: List<ContentImage>,
        travelJournalContentInformation: TravelJournalContentInformation,
    ): TravelJournalContent {
        val travelJournalContentImages =
            contentImages.map { contentImage -> TravelJournalContentImage(contentImage = contentImage) }
        return TravelJournalContent(
            travelJournalContentInformation = travelJournalContentInformation,
            travelJournalContentImages = travelJournalContentImages,
        )
    }

    private fun getContentImages(
        contentImageNames: List<String>,
        contentImageMap: Map<String, ContentImage>?,
        placeDetailsMap: Map<String, PlaceDetails>,
        placeId: String? = null,
    ) = contentImageNames
        .filter { contentImageMap?.contains(it) ?: false }
        .mapNotNull { contentImageMap?.getValue(it) }
        .apply {
            if (contentImageNames.isNotEmpty() && placeId != null) { // 여행일지 day에 장소가 태그되었다면 썸네일에 장소 정보id와 좌표 저장해서 나중에 지도에 뿌릴수 있도록 한다
                get(0) // 첫번째 사진이 대표사진, 썸네일로 사용된다
                    .setPlace(placeDetailsMap.getValue(placeId))
            }
        }

    private fun getContentImageMap(
        register: User,
        imageNamePairs: List<Pair<String, String>>,
    ): Map<String, ContentImage> {
        return imageNamePairs.associate { (fileName, originName) ->
            val contentImage = ContentImage(name = fileName, originName = originName, user = register)
            originName to contentImage
        }
    }

    private fun getTravelCompanions(
        travelCompanionIds: List<Long>?,
        travelCompanionNames: List<String>?,
    ): List<TravelCompanion> =
        if (travelCompanionIds == null && travelCompanionNames == null) {
            emptyList()
        } else {
            val users = travelCompanionIds?.let { userRepository.getByUserIds(it) } ?: emptyList()
            val travelCompanionsFromIds = users.map { TravelCompanion(user = it, username = null) }
            val travelCompanionsFromNames =
                travelCompanionNames.orEmpty().map { TravelCompanion(user = null, username = it) }
            travelCompanionsFromIds + travelCompanionsFromNames
        }

    private fun getTravelJournalContentImageNames(travelJournal: TravelJournal): List<String> =
        travelJournal.travelJournalContents.flatMap { travelJournalContent ->
            travelJournalContent.travelJournalContentImages.map { travelJournalContentImage ->
                travelJournalContentImage.contentImage.name
            }
        }

    private fun getRemoveTravelCompanionId(user: User, travelJournalId: Long): Long {
        return travelJournalRepository.findTravelCompanionId(user, travelJournalId) ?: throw ForbiddenException("요청 사용자(${user.id})는 해당 여행일지($travelJournalId)의 같이 간 친구를 처리할 권한이 없습니다.")
    }
}
