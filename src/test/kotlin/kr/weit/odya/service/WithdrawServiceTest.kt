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
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.security.FirebaseTokenHelper
import kr.weit.odya.support.DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createUser

class WithdrawServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val favoritePlaceRepository = mockk<FavoritePlaceRepository>()
        val favoriteTopicRepository = mockk<FavoriteTopicRepository>()
        val firebaseTokenHelper = mockk<FirebaseTokenHelper>()
        val travelJournalService = mockk<TravelJournalService>()
        val travelJournalBookmarkRepository = mockk<TravelJournalBookmarkRepository>()
        val placeReviewService = mockk<PlaceReviewService>()
        val userService = mockk<UserService>()
        val agreedTermsRepository = mockk<AgreedTermsRepository>()
        val communityService = mockk<CommunityService>()
        val withdrawService =
            WithdrawService(
                agreedTermsRepository,
                favoritePlaceRepository,
                favoriteTopicRepository,
                firebaseTokenHelper,
                placeReviewService,
                travelJournalService,
                travelJournalBookmarkRepository,
                userRepository,
                userService,
                communityService,
            )

        describe("withdrawUser") {
            context("유효한 토큰이 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { favoritePlaceRepository.deleteByUserId(TEST_USER_ID) } just runs
                every { placeReviewService.deleteReviewRelatedData(TEST_USER_ID) } just runs
                every { favoriteTopicRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { agreedTermsRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { firebaseTokenHelper.withdrawUser(TEST_USERNAME) } just runs
                every { travelJournalService.deleteTravelJournalByUserId(TEST_USER_ID) } just runs
                every { travelJournalBookmarkRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { userService.deleteUserRelatedData(TEST_USER_ID) } just runs
                every { communityService.deleteCommunityByUserId(TEST_USER_ID) } just runs
                it("정상적으로 종료한다") {
                    shouldNotThrowAny { withdrawService.withdrawUser(TEST_USER_ID) }
                }
            }

            context("존재하지 않는 유저ID가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환") {
                    shouldThrow<NoSuchElementException> { withdrawService.withdrawUser(TEST_USER_ID) }
                }
            }

            context("프로필이 Object Storage에 존재하지 않는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { favoritePlaceRepository.deleteByUserId(TEST_USER_ID) } just runs
                every { placeReviewService.deleteReviewRelatedData(TEST_USER_ID) } just runs
                every { favoriteTopicRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { agreedTermsRepository.deleteAllByUserId(TEST_USER_ID) } just runs
                every { communityService.deleteCommunityByUserId(TEST_USER_ID) } just runs
                every { travelJournalService.deleteTravelJournalByUserId(TEST_USER_ID) } just runs
                every { userService.deleteUserRelatedData(TEST_USER_ID) } throws IllegalArgumentException(
                    DELETE_NOT_EXIST_PROFILE_ERROR_MESSAGE,
                )
                it("[IllegalArgumentException] 반환") {
                    shouldThrow<IllegalArgumentException> { withdrawService.withdrawUser(TEST_USER_ID) }
                }
            }
        }
    },
)
