package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.client.GoogleMapsClient
import kr.weit.odya.client.push.NotificationEventType
import kr.weit.odya.client.push.PushNotificationEvent
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.community.CommunityDeleteEvent
import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.CommunitySortType
import kr.weit.odya.domain.community.CommunityUpdateEvent
import kr.weit.odya.domain.community.CommunityVisibility
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.community.getCommunityByTopic
import kr.weit.odya.domain.community.getCommunitySliceBy
import kr.weit.odya.domain.community.getFriendCommunitySliceBy
import kr.weit.odya.domain.community.getMyCommunitySliceBy
import kr.weit.odya.domain.communitycomment.CommunityComment
import kr.weit.odya.domain.communitycomment.CommunityCommentRepository
import kr.weit.odya.domain.communitycomment.deleteCommunityComments
import kr.weit.odya.domain.communitycomment.getCommunityWithCommentSliceBy
import kr.weit.odya.domain.communitylike.CommunityLikeRepository
import kr.weit.odya.domain.communitylike.deleteCommunityLikes
import kr.weit.odya.domain.communitylike.getLikedCommunitySliceBy
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowerFcmTokens
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.profilecolor.NONE_PROFILE_COLOR_HEX
import kr.weit.odya.domain.report.ReportCommunityRepository
import kr.weit.odya.domain.report.deleteAllByUserId
import kr.weit.odya.domain.topic.TopicRepository
import kr.weit.odya.domain.topic.getByTopicId
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalVisibility
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.CommunityContentImageResponse
import kr.weit.odya.service.dto.CommunityCreateRequest
import kr.weit.odya.service.dto.CommunityResponse
import kr.weit.odya.service.dto.CommunitySimpleResponse
import kr.weit.odya.service.dto.CommunitySummaryResponse
import kr.weit.odya.service.dto.CommunityUpdateRequest
import kr.weit.odya.service.dto.CommunityWithCommentsResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TravelJournalSimpleResponse
import kr.weit.odya.service.dto.UserProfileResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

private const val MAX_COMMUNITY_CONTENT_IMAGE_COUNT = 15
private const val MIM_COMMUNITY_CONTENT_IMAGE_COUNT = 1

@Service
class CommunityService(
    private val communityRepository: CommunityRepository,
    private val communityCommentRepository: CommunityCommentRepository,
    private val communityLikeRepository: CommunityLikeRepository,
    private val topicRepository: TopicRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val userRepository: UserRepository,
    private val fileService: FileService,
    private val eventPublisher: ApplicationEventPublisher,
    private val followRepository: FollowRepository,
    private val googleMapsClient: GoogleMapsClient,
    private val reportCommunityRepository: ReportCommunityRepository,
) {
    @Transactional
    fun createCommunity(
        userId: Long,
        request: CommunityCreateRequest,
        contentImagePairs: List<Pair<String, String>>,
    ): Long {
        val user = userRepository.getByUserId(userId)
        val travelJournal = getTravelJournalByRequest(request, userId)
        val topic = request.topicId?.let { topicRepository.getByTopicId(it) }
        val communityContentImages = getCommunityContentImages(contentImagePairs, user, request.placeId)
        val community = request.toEntity(user, topic, travelJournal, communityContentImages)
        val savedCommunity = communityRepository.save(community)
        publishFeedPushEvent(user, community)
        return savedCommunity.id
    }

    fun uploadContentImages(contentImages: List<MultipartFile>): List<Pair<String, String>> {
        return contentImages.map {
            require(it.originalFilename != null) { "파일 원본 이름은 필수 값입니다." }
            val fileName = fileService.saveFile(it)
            fileName to it.originalFilename!!
        }
    }

    @Transactional(readOnly = true)
    fun getCommunity(communityId: Long, userId: Long): CommunityResponse {
        val community = communityRepository.getByCommunityId(communityId)
        validateUserReadPermission(community, userId)
        val profileUrl = fileService.getPreAuthenticatedObjectUrl(community.user.profile.profileName)
        val travelJournalSimpleResponse = getTravelJournalSimpleResponse(community)
        val communityContentImages = getCommunityContentImageResponses(community)
        val communityCommentCount = communityCommentRepository.countByCommunityId(communityId)
        val isUserLiked = communityLikeRepository.existsByCommunityIdAndUserId(communityId, userId)
        val isFollowing =
            followRepository.existsByFollowerIdAndFollowingId(userId, community.user.id)
        return CommunityResponse.from(
            community,
            profileUrl,
            userId == community.user.id,
            travelJournalSimpleResponse,
            communityContentImages,
            communityCommentCount,
            isUserLiked,
            isFollowing,
        )
    }

    @Transactional(readOnly = true)
    fun getCommunities(
        userId: Long,
        size: Int,
        lastId: Long?,
        placeId: String?,
        sortType: CommunitySortType,
    ): SliceResponse<CommunitySummaryResponse> {
        val communities = communityRepository.getCommunitySliceBy(userId, size, lastId, placeId, sortType)
        return getCommunitySummarySliceResponse(size, communities, userId)
    }

    @Transactional(readOnly = true)
    fun getMyCommunities(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): SliceResponse<CommunitySimpleResponse> {
        val communities = communityRepository.getMyCommunitySliceBy(userId, size, lastId, sortType)
        return getCommunitySimpleSliceResponse(size, communities)
    }

    @Transactional(readOnly = true)
    fun getFriendCommunities(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): SliceResponse<CommunitySummaryResponse> {
        val communities = communityRepository.getFriendCommunitySliceBy(userId, size, lastId, sortType)
        return getCommunitySummarySliceResponse(size, communities, userId)
    }

    @Transactional(readOnly = true)
    fun searchByTopic(
        userId: Long,
        topicId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): SliceResponse<CommunitySummaryResponse> {
        val topic = topicRepository.getByTopicId(topicId)
        val communities = communityRepository.getCommunityByTopic(topic, size, lastId, sortType)
        return getCommunitySummarySliceResponse(size, communities, userId)
    }

    @Transactional(readOnly = true)
    fun getLikedCommunities(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: CommunitySortType,
    ): SliceResponse<CommunitySimpleResponse> {
        val communities = communityLikeRepository.getLikedCommunitySliceBy(userId, size, lastId).map { it.community }
        return getCommunitySimpleSliceResponse(size, communities)
    }

    @Transactional(readOnly = true)
    fun getCommunityWithComments(
        userId: Long,
        size: Int,
        lastId: Long?,
    ): SliceResponse<CommunityWithCommentsResponse> {
        val communityComments = communityCommentRepository.getCommunityWithCommentSliceBy(userId, size, lastId)
        return getCommunityWithCommentSliceResponse(size, communityComments)
    }

    @Transactional(readOnly = true)
    fun validateUpdateCommunityRequest(
        communityId: Long,
        userId: Long,
        deleteCommunityContentImageIds: List<Long>?,
        updateImageSize: Int,
    ) {
        val community = communityRepository.getByCommunityId(communityId)
        validateUserPermission(community.user.id, userId)

        val deleteCommunityContentImageSize = deleteCommunityContentImageIds?.size ?: 0
        val communityContentImageTotalCount =
            community.communityContentImages.size + updateImageSize - deleteCommunityContentImageSize
        require(communityContentImageTotalCount in MIM_COMMUNITY_CONTENT_IMAGE_COUNT..MAX_COMMUNITY_CONTENT_IMAGE_COUNT) {
            "커뮤니티 사진은 최소 $MIM_COMMUNITY_CONTENT_IMAGE_COUNT 개 이상, 최대 $MAX_COMMUNITY_CONTENT_IMAGE_COUNT 개 이하로 업로드할 수 있습니다."
        }
    }

    @Transactional
    fun updateCommunity(
        communityId: Long,
        userId: Long,
        communityUpdateRequest: CommunityUpdateRequest,
        contentImagePairs: List<Pair<String, String>>,
    ) {
        val community = communityRepository.getByCommunityId(communityId)

        // 기본 내용 업데이트
        val updateTravelJournal = getUpdateTravelJournal(communityUpdateRequest, userId, community)
        val updateTopic = getUpdateTopic(communityUpdateRequest, community)
        community.updateCommunity(communityUpdateRequest.toCommunityInformation(), updateTravelJournal, updateTopic)

        // 사진 업데이트
        val updateCommunityContentImages = getUpdateCommunityContentImages(contentImagePairs, community)
        community.addCommunityContentImages(updateCommunityContentImages)

        // 사진 삭제
        val deleteCommunityContentImages = getCommunityContentImageByDeleteIds(communityUpdateRequest, community)
        community.deleteCommunityContentImages(deleteCommunityContentImages)
        eventPublisher.publishEvent(
            CommunityUpdateEvent(
                getDeleteCommunityContentImageNames(
                    deleteCommunityContentImages,
                ),
            ),
        )
    }

    @Transactional
    fun deleteCommunity(communityId: Long, userId: Long) {
        val community = communityRepository.getByCommunityId(communityId)
        validateUserPermission(community.user.id, userId)
        val deleteCommunityContentImageNames = getDeleteCommunityContentImageNames(community.communityContentImages)
        communityLikeRepository.deleteAllByCommunityId(communityId)
        communityCommentRepository.deleteAllByCommunityId(communityId)
        communityRepository.delete(community)
        eventPublisher.publishEvent(CommunityDeleteEvent(deleteCommunityContentImageNames))
    }

    @Transactional
    fun deleteCommunityByUserId(userId: Long) {
        reportCommunityRepository.deleteAllByUserId(userId)
        communityLikeRepository.deleteCommunityLikes(userId)
        communityCommentRepository.deleteCommunityComments(userId)
        communityRepository.deleteAllByUserId(userId)
    }

    private fun getDeleteCommunityContentImageNames(deleteCommunityContentImages: List<CommunityContentImage>) =
        deleteCommunityContentImages.map { it.contentImage.name }

    private fun getCommunityContentImageByDeleteIds(
        communityUpdateRequest: CommunityUpdateRequest,
        community: Community,
    ) = communityUpdateRequest.deleteCommunityContentImageIds?.let {
        community.communityContentImages.filter { communityContentImage -> it.contains(communityContentImage.id) }
    } ?: emptyList()

    private fun getUpdateCommunityContentImages(
        contentImagePairs: List<Pair<String, String>>,
        community: Community,
    ) = contentImagePairs.map { (name, originName) ->
        val contentImage = ContentImage(name = name, originName = originName, user = community.user)
        CommunityContentImage(contentImage = contentImage)
    }

    private fun getUpdateTopic(
        communityUpdateRequest: CommunityUpdateRequest,
        community: Community,
    ) = communityUpdateRequest.topicId?.let {
        topicRepository.getByTopicId(it)
    } ?: community.topic

    private fun getUpdateTravelJournal(
        communityUpdateRequest: CommunityUpdateRequest,
        userId: Long,
        community: Community,
    ) = communityUpdateRequest.travelJournalId?.let {
        val travelJournal = travelJournalRepository.getByTravelJournalId(it)
        validateUserPermission(travelJournal.user.id, userId)
        travelJournal
    } ?: community.travelJournal

    private fun validateUserPermission(writerId: Long, userId: Long) {
        if (writerId != userId) {
            throw ForbiddenException("요청 사용자($userId)는 해당 요청을 처리할 권한이 없습니다.")
        }
    }

    private fun getCommunitySimpleSliceResponse(
        size: Int,
        contents: List<Community>,
    ) = SliceResponse(
        size = size,
        content = contents.map { community ->
            val communityMainImageUrl =
                fileService.getPreAuthenticatedObjectUrl(community.communityContentImages[0].contentImage.name)
            CommunitySimpleResponse.from(
                community,
                communityMainImageUrl,
            )
        },
    )

    private fun getCommunitySummarySliceResponse(
        size: Int,
        contents: List<Community>,
        userId: Long,
    ) = SliceResponse(
        size = size,
        content = contents.map { community ->
            val communityMainImageUrl =
                fileService.getPreAuthenticatedObjectUrl(community.communityContentImages[0].contentImage.name)
            val writerProfileUrl =
                fileService.getPreAuthenticatedObjectUrl(community.user.profile.profileName)
            val isFollowing = followRepository.existsByFollowerIdAndFollowingId(userId, community.user.id)
            val communityCommentCount = communityCommentRepository.countByCommunityId(community.id)
            val isUserLiked = communityLikeRepository.existsByCommunityIdAndUserId(community.id, userId)
            CommunitySummaryResponse.from(
                community,
                communityMainImageUrl,
                writerProfileUrl,
                isFollowing,
                communityCommentCount,
                isUserLiked,
            )
        },
    )

    private fun getCommunityWithCommentSliceResponse(
        size: Int,
        contents: List<CommunityComment>,
    ): SliceResponse<CommunityWithCommentsResponse> =
        SliceResponse(
            size = size,
            content = contents.map { comment ->
                val communityMainImageUrl =
                    fileService.getPreAuthenticatedObjectUrl(comment.community.communityContentImages[0].contentImage.name)
                val writerProfileUrl =
                    fileService.getPreAuthenticatedObjectUrl(comment.community.user.profile.profileName)
                val commenterProfileUrl =
                    fileService.getPreAuthenticatedObjectUrl(comment.user.profile.profileName)
                val followingIdList = followRepository.getFollowingIds(comment.user.id)
                CommunityWithCommentsResponse.from(
                    comment.community,
                    communityMainImageUrl,
                    writerProfileUrl,
                    comment,
                    commenterProfileUrl,
                    followingIdList,
                )
            },
        )

    private fun getTravelJournalSimpleResponse(community: Community): TravelJournalSimpleResponse? =
        community.travelJournal?.let {
            val mainImageAuthenticatedUrl =
                fileService.getPreAuthenticatedObjectUrl(it.travelJournalContents[0].travelJournalContentImages[0].contentImage.name)
            TravelJournalSimpleResponse(it.id, it.title, mainImageAuthenticatedUrl)
        }

    private fun getCommunityContentImageResponses(community: Community) =
        community.communityContentImages.map {
            CommunityContentImageResponse(
                it.id,
                fileService.getPreAuthenticatedObjectUrl(it.contentImage.name),
            )
        }

    private fun validateUserReadPermission(community: Community, userId: Long) {
        if (community.communityInformation.visibility == CommunityVisibility.FRIEND_ONLY &&
            community.user.id != userId && !followRepository.existsByFollowerIdAndFollowingId(community.user.id, userId)
        ) {
            throw ForbiddenException("친구가 아닌 사용자($userId)는 친구에게만 공개하는 여행 일지(${community.id})를 조회할 수 없습니다.")
        }
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
                userName = user.nickname,
                userProfileUrl = fileService.getPreAuthenticatedObjectUrl(user.profile.profileName),
                userProfileColor = if (user.profile.profileColor.colorHex != NONE_PROFILE_COLOR_HEX) {
                    UserProfileResponse.ProfileColorResponse(user.profile.profileColor)
                } else {
                    null
                },
                contentImage = fileService.getPreAuthenticatedObjectUrl(community.communityContentImages[0].contentImage.name),
                eventType = NotificationEventType.FOLLOWING_COMMUNITY,
                communityId = community.id,
            ),
        )
    }

    private fun validateTravelJournal(travelJournal: TravelJournal, userId: Long) {
        require(travelJournal.visibility != TravelJournalVisibility.PRIVATE) {
            "비공개 여행일지는 커뮤니티와 연결할 수 없습니다."
        }
        if (travelJournal.user.id != userId) {
            throw ForbiddenException("요청 사용자($userId)는 여행 일지를 커뮤니티에 연결할 권한이 없습니다.")
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

    private fun getTravelJournalByRequest(request: CommunityCreateRequest, userId: Long): TravelJournal? =
        request.travelJournalId?.let { travelJournalRepository.getByTravelJournalId(it) }?.apply {
            validateTravelJournal(this, userId)
        }
}
