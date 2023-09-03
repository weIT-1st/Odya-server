package kr.weit.odya.service

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
    private val firebaseCloudMessageService: FirebaseCloudMessageService,
    private val followRepository: FollowRepository,
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
        val communityContentImages = getCommunityContentImages(contentImagePairs, user)
        val community = request.toEntity(user, topic, travelJournal, communityContentImages)
        communityRepository.save(community)
        sendPushNotification(user, community)
    }

    private fun sendPushNotification(
        user: User,
        community: Community,
    ) {
        followRepository.getFollowerFcmTokens(user.id).let { fcmTokens ->
            firebaseCloudMessageService.sendPushNotification(
                PushNotificationRequest(
                    title = "피드 알림",
                    body = "${user.nickname}님이 피드를 작성했어요!",
                    tokens = fcmTokens,
                    data = mapOf("communityId" to community.id.toString()),
                ),
            )
        }
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

    private fun getCommunityContentImages(contentImagePairs: List<Pair<String, String>>, user: User) =
        contentImagePairs.map { (name, originName) ->
            val contentImage = ContentImage(name = name, originName = originName, user = user)
            CommunityContentImage(contentImage = contentImage)
        }
}
