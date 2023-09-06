package kr.weit.odya.service

import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowerFcmTokens
import kr.weit.odya.domain.traveljournal.TravelCompanion
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalContent
import kr.weit.odya.domain.traveljournal.TravelJournalContentImage
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.service.dto.TravelJournalContentRequest
import kr.weit.odya.service.dto.TravelJournalRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

private const val MAX_TRAVEL_DAYS = 15

private const val MAX_TRAVEL_COMPANION_COUNT = 10

@Service
class TravelJournalService(
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val fileService: FileService,
    private val followRepository: FollowRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun createTravelJournal(
        userId: Long,
        travelJournalRequest: TravelJournalRequest,
        imageNamePairs: List<Pair<String, String>>,
    ) {
        val register = userRepository.getByUserId(userId)
        val travelCompanions =
            getTravelCompanions(travelJournalRequest.travelCompanionIds, travelJournalRequest.travelCompanionNames)
        val contentImageMap = getContentImageMap(register, imageNamePairs)
        val travelJournalContents = travelJournalRequest.travelJournalContentRequests.map { travelJournalContent ->
            val contentImages = getContentImages(travelJournalContent, contentImageMap)
            getTravelJournalContent(contentImages, travelJournalContent)
        }
        val travelJournal = travelJournalRequest.toEntity(register, travelCompanions, travelJournalContents)
        travelJournalRepository.save(travelJournal)
        publishTravelJournalPushEvent(register, travelJournal)
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

    fun uploadTravelContentImages(
        travelJournalRequest: TravelJournalRequest,
        imageMap: Map<String, MultipartFile>?,
    ): List<Pair<String, String>> = travelJournalRequest.travelJournalContentRequests.flatMap { travelJournalContent ->
        travelJournalContent.contentImageNames.orEmpty().mapNotNull { contentImageName ->
            imageMap?.getValue(contentImageName)?.let { image ->
                val fileName = fileService.saveFile(image)
                fileName to image.originalFilename!!
            }
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

    fun getImageMap(images: List<MultipartFile>): Map<String, MultipartFile>? =
        images.associateBy {
            require(!it.originalFilename.isNullOrEmpty()) { IllegalArgumentException("파일 원본 이름은 필수 값입니다.") }
            it.originalFilename!!
        }

    private fun getTravelJournalContent(
        contentImages: List<ContentImage>,
        travelJournalContentRequest: TravelJournalContentRequest,
    ): TravelJournalContent {
        val travelJournalContentImages =
            contentImages.map { contentImage -> TravelJournalContentImage(contentImage = contentImage) }
        return travelJournalContentRequest.toEntity(travelJournalContentImages)
    }

    private fun getContentImages(
        travelJournalContent: TravelJournalContentRequest,
        contentImageMap: Map<String, ContentImage>?,
    ) = travelJournalContent.contentImageNames
        .filter { contentImageMap?.contains(it) ?: false }
        .mapNotNull { contentImageMap?.getValue(it) }

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
}
