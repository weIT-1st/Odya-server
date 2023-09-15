package kr.weit.odya.service

import kr.weit.odya.domain.agreedTerms.AgreedTermsRepository
import kr.weit.odya.domain.favoritePlace.FavoritePlaceRepository
import kr.weit.odya.domain.favoriteTopic.FavoriteTopicRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.security.FirebaseTokenHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WithdrawService(
    private val agreedTermsRepository: AgreedTermsRepository,
    private val favoritePlaceRepository: FavoritePlaceRepository,
    private val favoriteTopicRepository: FavoriteTopicRepository,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val placeReviewService: PlaceReviewService,
    private val travelJournalService: TravelJournalService,
    private val userRepository: UserRepository,
    private val userService: UserService,
) {
    @Transactional
    fun withdrawUser(userId: Long) {
        val uid = userRepository.getByUserId(userId).username
        favoritePlaceRepository.deleteByUserId(userId)
        placeReviewService.deleteReviewRelatedData(userId)
        favoriteTopicRepository.deleteAllByUserId(userId)
        agreedTermsRepository.deleteAllByUserId(userId)
        travelJournalService.deleteTravelJournalByUserId(userId)
        // TODO: 커뮤니티 삭제
        userService.deleteUserRelatedData(userId)
        firebaseTokenHelper.withdrawUser(uid)
    }
}
