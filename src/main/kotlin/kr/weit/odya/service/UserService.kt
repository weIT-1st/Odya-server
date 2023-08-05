package kr.weit.odya.service

import kr.weit.odya.domain.favoritePlace.FavoritePlaceRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.user.DEFAULT_PROFILE_PNG
import kr.weit.odya.domain.user.ProfileRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.existsByEmail
import kr.weit.odya.domain.user.existsByNickname
import kr.weit.odya.domain.user.existsByPhoneNumber
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.domain.user.getByUserIdWithProfile
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.service.dto.InformationRequest
import kr.weit.odya.service.dto.KakaoWithdrawRequest
import kr.weit.odya.service.dto.UserResponse
import kr.weit.odya.service.generator.FileNameGenerator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.InputStream
import java.util.Locale

val ALLOW_FILE_FORMAT_LIST: List<String> = listOf("png", "jpg", "jpeg", "webp")

@Service
class UserService(
    private val userRepository: UserRepository,
    private val favoritePlaceRepository: FavoritePlaceRepository,
    private val followRepository: FollowRepository,
    private val placeReviewRepository: PlaceReviewRepository,
    private val profileRepository: ProfileRepository,
    private val objectStorageService: ObjectStorageService,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val fileNameGenerator: FileNameGenerator,
    private val profileColorService: ProfileColorService,
) {
    fun getEmailByIdToken(idToken: String) = firebaseTokenHelper.getEmail(idToken)

    fun getPhoneNumberByIdToken(idToken: String) = firebaseTokenHelper.getPhoneNumber(idToken)

    @Transactional(readOnly = true)
    fun getInformation(userId: Long): UserResponse {
        val findUser = userRepository.getByUserIdWithProfile(userId)
        val profileUrl = objectStorageService.getPreAuthenticatedObjectUrl(findUser.profile.profileName)
        return UserResponse(findUser, profileUrl)
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
    }

    fun uploadProfile(inputStream: InputStream, originalFilename: String?): String {
        val fileFormat = getFileFormat(originalFilename)
        val profileName = "${fileNameGenerator.generate()}.$fileFormat"
        objectStorageService.save(inputStream, profileName)
        return profileName
    }

    fun deleteProfile(userId: Long) {
        val profileName = userRepository.getByUserIdWithProfile(userId).profile.profileName.also {
            require(it != DEFAULT_PROFILE_PNG) { "기본 프로필은 삭제할 수 없습니다" }
        }
        objectStorageService.delete(profileName)
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

    @Transactional
    fun withdraw(kakaoWithdrawRequest: KakaoWithdrawRequest, userId: Long) {
        runCatching {
            favoritePlaceRepository.deleteAll(favoritePlaceRepository.findAllByUserId(userId))
            followRepository.deleteAll(followRepository.findAllByFollowerId(userId))
            followRepository.deleteAll(followRepository.findAllByFollowingId(userId))
            placeReviewRepository.deleteAll(placeReviewRepository.findAllByUserId(userId))
            profileRepository.deleteById(userId)
            userRepository.deleteById(userId)
        }.onFailure {
            throw RuntimeException("회원 탈퇴 중 오류가 발생했습니다")
        }
        firebaseTokenHelper.withdrawUser(kakaoWithdrawRequest.accessToken)
    }

    private fun validateInformationRequest(informationRequest: InformationRequest) {
        if (userRepository.existsByNickname(informationRequest.nickname)) {
            throw ExistResourceException("${informationRequest.nickname}: 이미 존재하는 닉네임입니다")
        }
    }

    private fun getFileFormat(originFileName: String?): String {
        require(originFileName != null) { "원본 파일 이름이 존재하지 않습니다" }
        return originFileName.let {
            it.substring(it.lastIndexOf(".") + 1).lowercase(Locale.getDefault()).apply {
                require(ALLOW_FILE_FORMAT_LIST.contains(this)) {
                    "프로필 사진은 ${ALLOW_FILE_FORMAT_LIST.joinToString()} 형식만 가능합니다"
                }
            }
        }
    }

    private fun getProfileColor(profileName: String?) = if (profileName == null) {
        profileColorService.getRandomProfileColor()
    } else {
        profileColorService.getNoneProfileColor()
    }
}
