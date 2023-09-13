package kr.weit.odya.service

import kr.weit.odya.client.GoogleMapsClient
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowerFcmTokens
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.CommunityCreateRequest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class CommunityService(
    private val communityRepository: CommunityRepository,
    private val topicRepository: TopicRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService,
    private val eventPublisher: ApplicationEventPublisher,
    private val followRepository: FollowRepository,
    private val googleMapsClient: GoogleMapsClient,
) {
    @Transactional
    fun createCommunity(
        userId: Long,
        request: CommunityCreateRequest,
        contentImagePairs: List<Pair<String, String>>,
    ) {
        val user = userRepository.getByUserId(userId)
        val travelJournal = getNonPrivateTravelJournal(request.travelJournalId)
        val topic = request.topicId?.let { topicRepository.getByTopicId(it) }
        val communityContentImages = getCommunityContentImages(contentImagePairs, user, request.placeId)
        val community = request.toEntity(user, topic, travelJournal, communityContentImages)
        communityRepository.save(community)
        publishFeedPushEvent(user, community)
    }

    private fun publishFeedPushEvent(
        user: User,
        community: Community,
    ) {
        val fcmTokens = followRepository.getFollowerFcmTokens(user.id)
        eventPublisher.publishEvent(
            PushNotificationEvent(
                title = "피드 알림",
                body = "${user.nickname}님이 피드를 작성했어요!",
                tokens = fcmTokens,
                data = mapOf("communityId" to community.id.toString()),
            ),
        )
    }

    fun uploadContentImages(contentImages: List<MultipartFile>): List<Pair<String, String>> {
        return contentImages.map {
            require(it.originalFilename != null) { "파일 원본 이름은 필수 값입니다." }
            val fileName = fileService.saveFile(it)
            fileName to it.originalFilename!!
        }
    }

    private fun getNonPrivateTravelJournal(travelJournalId: Long?): TravelJournal? =
        travelJournalId?.let { travelJournalRepository.getByTravelJournalId(it) }?.apply {
            require(visibility != TravelJournalVisibility.PRIVATE) {
                "비공개 여행일지는 커뮤니티와 연결할 수 없습니다."
            }
        }

    private fun getCommunityContentImages(contentImagePairs: List<Pair<String, String>>, user: User, placeId: String?) =
        contentImagePairs.map { (name, originName) ->
            val contentImage = ContentImage(name = name, originName = originName, user = user)
            CommunityContentImage(contentImage = contentImage)
        }.apply {
            if (placeId != null) { // 피드에 장소가 태그되었다면 썸네일에 장소 정보id와 좌표 저장해서 나중에 지도에 뿌릴수 있도록 한다
                get(0).contentImage // 첫번째 사진이 대표사진, 썸네일로 사용된다
                    .setPlace(googleMapsClient.findPlaceDetailsByPlaceId(placeId))
            }
        }
}
