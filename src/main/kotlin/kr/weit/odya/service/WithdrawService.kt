package kr.weit.odya.service

import kr.weit.odya.domain.agreedTerms.AgreedTermsRepository
import kr.weit.odya.domain.favoritePlace.FavoritePlaceRepository
import kr.weit.odya.domain.favoriteTopic.FavoriteTopicRepository
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.placeReview.PlaceReviewRepository
import kr.weit.odya.domain.placeReview.getByUserId
import kr.weit.odya.domain.report.ReportPlaceReviewRepository
import kr.weit.odya.domain.report.ReportTravelJournalRepository
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByUserId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.security.FirebaseTokenHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WithdrawService(
    private val userRepository: UserRepository,
    private val favoritePlaceRepository: FavoritePlaceRepository,
    private val followRepository: FollowRepository,
    private val placeReviewRepository: PlaceReviewRepository,
    private val favoriteTopicRepository: FavoriteTopicRepository,
    private val firebaseTokenHelper: FirebaseTokenHelper,
    private val usersDocumentRepository: UsersDocumentRepository,
    private val agreedTermsRepository: AgreedTermsRepository,
    private val reportPlaceReviewRepository: ReportPlaceReviewRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val reportTravelJournalRepository: ReportTravelJournalRepository,
) {
    @Transactional
    fun withdrawUser(userId: Long) {
        val uid = userRepository.getByUserId(userId).username
        favoritePlaceRepository.deleteByUserId(userId)
        followRepository.deleteByFollowingId(userId)
        followRepository.deleteByFollowerId(userId)
        reportPlaceReviewRepository.deleteAllByUserId(userId)
        reportPlaceReviewRepository.deleteAllByPlaceReviewIn(placeReviewRepository.getByUserId(userId))
        placeReviewRepository.deleteByUserId(userId)
        favoriteTopicRepository.deleteByUserId(userId)
        agreedTermsRepository.deleteByUserId(userId)
        reportTravelJournalRepository.deleteAllByUserId(userId)
        reportTravelJournalRepository.deleteAllByTravelJournalIn(travelJournalRepository.getByUserId(userId))
        travelJournalRepository.deleteAllByUserId(userId)
        userRepository.deleteById(userId)
        usersDocumentRepository.deleteById(userId)
        firebaseTokenHelper.withdrawUser(uid)
    }
}
