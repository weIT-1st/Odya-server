package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createPlaceReview
import kr.weit.odya.support.createTravelJournal
import kr.weit.odya.support.createUser

class WithdrawServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val favoritePlaceRepository = mockk<FavoritePlaceRepository>()
        val followRepository = mockk<FollowRepository>()
        val placeReviewRepository = mockk<PlaceReviewRepository>()
        val favoriteTopicRepository = mockk<FavoriteTopicRepository>()
        val firebaseTokenHelper = mockk<FirebaseTokenHelper>()
        val usersDocumentRepository = mockk<UsersDocumentRepository>()
        val agreedTermsRepository = mockk<AgreedTermsRepository>()
        val reportPlaceReviewRepository = mockk<ReportPlaceReviewRepository>()
        val reportTravelJournalRepository = mockk<ReportTravelJournalRepository>()
        val travelJournalRepository = mockk<TravelJournalRepository>()
        val withdrawService =
            WithdrawService(
                userRepository,
                favoritePlaceRepository,
                followRepository,
                placeReviewRepository,
                favoriteTopicRepository,
                firebaseTokenHelper,
                usersDocumentRepository,
                agreedTermsRepository,
                reportPlaceReviewRepository,
                travelJournalRepository,
                reportTravelJournalRepository,
            )

        describe("withdrawUser") {
            context("유효한 토큰이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { favoritePlaceRepository.deleteByUserId(any()) } just runs
                every { followRepository.deleteByFollowerId(any()) } just runs
                every { followRepository.deleteByFollowingId(any()) } just runs
                every { placeReviewRepository.deleteByUserId(any()) } just runs
                every { favoriteTopicRepository.deleteByUserId(any()) } just runs
                every { userRepository.deleteById(TEST_USER_ID) } just runs
                every { firebaseTokenHelper.withdrawUser(TEST_USERNAME) } just runs
                every { usersDocumentRepository.deleteById(TEST_USER_ID) } just runs
                every { agreedTermsRepository.deleteByUserId(TEST_USER_ID) } just runs
                every { placeReviewRepository.getByUserId(TEST_USER_ID) } returns listOf(createPlaceReview())
                every { reportPlaceReviewRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { reportPlaceReviewRepository.deleteAllByPlaceReviewIn(any()) } just runs
                every { travelJournalRepository.getByUserId(TEST_USER_ID) } returns listOf(createTravelJournal())
                every { travelJournalRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { reportTravelJournalRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { reportTravelJournalRepository.deleteAllByTravelJournalIn(any()) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { withdrawService.withdrawUser(TEST_USER_ID) }
                }
            }

            context("존재하지 않는 유저ID가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                every { favoritePlaceRepository.deleteByUserId(any()) } just runs
                every { followRepository.deleteByFollowerId(any()) } just runs
                every { followRepository.deleteByFollowingId(any()) } just runs
                every { placeReviewRepository.deleteByUserId(any()) } just runs
                every { favoriteTopicRepository.deleteByUserId(any()) } just runs
                every { userRepository.deleteById(TEST_USER_ID) } just runs
                every { firebaseTokenHelper.withdrawUser(TEST_USERNAME) } just runs
                every { usersDocumentRepository.deleteById(TEST_USER_ID) } just runs
                every { agreedTermsRepository.deleteByUserId(TEST_USER_ID) } just runs
                it("[NoSuchElementException] 반환") {
                    shouldThrow<NoSuchElementException> { withdrawService.withdrawUser(TEST_USER_ID) }
                }
            }
        }
    },
)
