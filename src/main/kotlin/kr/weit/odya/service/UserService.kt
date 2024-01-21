package kr.weit.odya.service

import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getAllByUserId
import kr.weit.odya.domain.contentimage.ContentImageRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByUserId
import kr.weit.odya.domain.user.DEFAULT_PROFILE_PNG
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocument
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.domain.user.getByNickname
import kr.weit.odya.domain.user.getByPhoneNumbers
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIdWithProfile
import kr.weit.odya.domain.user.getByUserIds
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.service.dto.FCMTokenRequest
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.SearchPhoneNumberRequest
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.UserResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import kr.weit.odya.service.dto.UserStatisticsResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class UserService(
    private val userRepository: UserRepository,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val fileService: FileService,
    private val profileColorService: ProfileColorService,
    private val usersDocumentRepository: UsersDocumentRepository,
    private val followRepository: FollowRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val communityRepository: CommunityRepository,
    private val contentImageRepository: ContentImageRepository,
) {
    fun getEmailByIdToken(idToken: String) = firebaseTokenHelper.getEmail(idToken)

    fun getPhoneNumberByIdToken(idToken: String) = firebaseTokenHelper.getPhoneNumber(idToken)

    @Transactional(readOnly = true)
    fun getInformation(userId: Long): UserResponse {
        val findUser = userRepository.getByUserIdWithProfile(userId)
        return getUserResponse(findUser)
    }

    @Transactional
    fun updateEmail(userId: Long, email: String) {
        if (userRepository.existsByEmail(email)) {
            throw ExistResourceException("$email: 이미 존재하는 이메일입니다")
        }

        val findUser = userRepository.getByUserId(userId)
        findUser.changeEmail(email)
    }

    @Transactional
    fun updatePhoneNumber(userId: Long, phoneNumber: String) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw ExistResourceException("$phoneNumber: 이미 존재하는 전화번호입니다")
        }

        val findUser = userRepository.getByUserId(userId)
        findUser.changePhoneNumber(phoneNumber)
    }

    @Transactional
    fun updateInformation(userId: Long, informationRequest: InformationRequest) {
        validateInformationRequest(informationRequest)
        val findUser = userRepository.getByUserId(userId)
        findUser.changeInformation(informationRequest.nickname)
        usersDocumentRepository.save(UsersDocument(findUser))
    }

    fun uploadProfile(profile: MultipartFile): String = fileService.saveFile(profile)

    fun deleteProfile(userId: Long) {
        val profileName = userRepository.getByUserIdWithProfile(userId).profile.profileName.also {
            require(it != DEFAULT_PROFILE_PNG) { "기본 프로필은 삭제할 수 없습니다" }
        }
        fileService.deleteFile(profileName)
    }

    @Transactional
    fun updateProfile(userId: Long, profileName: String?, originalFileName: String?) {
        userRepository.getByUserIdWithProfile(userId).apply {
            this.changeProfile(
                profileName ?: DEFAULT_PROFILE_PNG,
                originalFileName ?: DEFAULT_PROFILE_PNG,
                getProfileColor(profileName),
            )
        }
    }

    fun searchByNickname(userId: Long, nickname: String, size: Int, lastId: Long?): SliceResponse<UserSimpleResponse> {
        val usersDocuments = usersDocumentRepository.getByNickname(nickname)
        val userIds = usersDocuments.map { it.id }
        val followingIdList = followRepository.getFollowingIds(userId)
        val users = userRepository.getByUserIds(userIds, size + 1, lastId).map {
            UserSimpleResponse(
                it,
                fileService.getPreAuthenticatedObjectUrl(it.profile.profileName),
                it.id in followingIdList,
            )
        }
        return SliceResponse(size, users)
    }

    @Transactional(readOnly = true)
    fun getStatistics(userId: Long): UserStatisticsResponse {
        val followingsCount = followRepository.countByFollowerId(userId)
        val followersCount = followRepository.countByFollowingId(userId)
        val travelJournals = travelJournalRepository.getByUserId(userId)
        val travelPlaceCount = travelJournals.sumOf { travelJournal ->
            travelJournal.travelJournalContents.count { content -> content.placeId != null }
        }
        val communityLikeCount = communityRepository.getAllByUserId(userId).sumOf { it.likeCount }
        val lifeShotCount = contentImageRepository.countByUserIdAndIsLifeShotIsTrue(userId)
        return UserStatisticsResponse(
            travelJournalCount = travelJournals.size,
            travelPlaceCount = travelPlaceCount,
            followingsCount = followingsCount,
            followersCount = followersCount,
            odyaCount = communityLikeCount,
            lifeShotCount = lifeShotCount,
        )
    }

    @Transactional
    fun updateFcmToken(userId: Long, fcmTokenRequest: FCMTokenRequest) {
        // FCM토큰이 충돌나는 경우 기존 유저의 토큰을 무효화 한다
        userRepository.findByFcmToken(fcmTokenRequest.fcmToken)?.changeFcmToken(null)
        val user = userRepository.getByUserId(userId)
        user.changeFcmToken(fcmTokenRequest.fcmToken)
    }

    @Transactional
    fun deleteUserRelatedData(id: Long) {
        val profileName = userRepository.getByUserIdWithProfile(id).profile.profileName
        followRepository.deleteByFollowingId(id)
        followRepository.deleteByFollowerId(id)
        userRepository.deleteById(id)
        usersDocumentRepository.deleteById(id)
        if (profileName != DEFAULT_PROFILE_PNG) {
            fileService.deleteFile(profileName)
        }
    }

    @Transactional(readOnly = true)
    fun searchByPhoneNumbers(userId: Long, phoneNumber: List<SearchPhoneNumberRequest>): List<UserSimpleResponse> {
        val users = userRepository.getByPhoneNumbers(phoneNumber.map { it.phoneNumber })
        val followings = followRepository.getFollowingIds(userId)
        return users.map {
            UserSimpleResponse(
                it,
                fileService.getPreAuthenticatedObjectUrl(it.profile.profileName),
                followings.contains(it.id),
            )
        }
    }

    private fun getProfileColor(profileName: String?) = if (profileName == null) {
        profileColorService.getRandomProfileColor()
    } else {
        profileColorService.getNoneProfileColor()
    }

    private fun validateInformationRequest(informationRequest: InformationRequest) {
        if (userRepository.existsByNickname(informationRequest.nickname)) {
            throw ExistResourceException("${informationRequest.nickname}: 이미 존재하는 닉네임입니다")
        }
    }

    private fun getUserResponse(findUser: User): UserResponse {
        val profileUrl = fileService.getPreAuthenticatedObjectUrl(findUser.profile.profileName)
        return UserResponse(findUser, profileUrl)
    }
}
