package kr.weit.odya.service

import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.follow.getFollowingIds
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalRepository
import kr.weit.odya.domain.representativetraveljournal.getRepTravelJournalIds
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmark
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkRepository
import kr.weit.odya.domain.traveljournalbookmark.TravelJournalBookmarkSortType
import kr.weit.odya.domain.traveljournalbookmark.getSliceBy
import kr.weit.odya.domain.traveljournalbookmark.getSliceByOther
import kr.weit.odya.domain.traveljournalbookmark.getTravelJournalIds
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TravelJournalBookmarkSummaryResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TravelJournalBookmarkService(
    private val travelJournalBookmarkRepository: TravelJournalBookmarkRepository,
    private val userRepository: UserRepository,
    private val travelJournalRepository: TravelJournalRepository,
    private val fileService: FileService,
    private val followRepository: FollowRepository,
    private val repTravelJournalRepository: RepresentativeTravelJournalRepository,
) {
    @Transactional
    fun createTravelJournalBookmark(
        userId: Long,
        travelJournalId: Long,
    ) {
        val user = userRepository.getByUserId(userId)
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        if (travelJournalBookmarkRepository.existsByUserAndTravelJournal(user, travelJournal)) {
            throw ExistResourceException("이미 사용자(${user.id})가 즐겨찾기한 여행일지(${travelJournal.id}) 입니다.")
        }

        travelJournalBookmarkRepository.save(
            TravelJournalBookmark(
                user = user,
                travelJournal = travelJournal,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getMyTravelJournalBookmarks(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
    ): SliceResponse<TravelJournalBookmarkSummaryResponse> {
        val user = userRepository.getByUserId(userId)
        val followingIdList = followRepository.getFollowingIds(userId)
        val repTravelJournalIds = repTravelJournalRepository.getRepTravelJournalIds(userId)
        val journalBookmarkResponses =
            travelJournalBookmarkRepository.getSliceBy(size, lastId, sortType, user).map { bookmark ->
                val profileUrl = fileService.getPreAuthenticatedObjectUrl(bookmark.user.profile.profileName)
                val travelJournalMainImageUrl =
                    fileService.getPreAuthenticatedObjectUrl(
                        bookmark.travelJournal.travelJournalContents[0].travelJournalContentImages[0].contentImage.name,
                    )
                TravelJournalBookmarkSummaryResponse.from(
                    bookmark,
                    profileUrl,
                    travelJournalMainImageUrl,
                    bookmark.travelJournal.user,
                    bookmark.user.id in followingIdList,
                    true,
                    bookmark.travelJournal.id in repTravelJournalIds,
                )
            }
        return SliceResponse(size, journalBookmarkResponses)
    }

    @Transactional(readOnly = true)
    fun getOtherTravelJournalBookmarks(
        loginUser: Long,
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: TravelJournalBookmarkSortType,
    ): SliceResponse<TravelJournalBookmarkSummaryResponse> {
        val user = userRepository.getByUserId(userId)
        val followingIdList = followRepository.getFollowingIds(loginUser)
        val bookmarkTravelJournalIdList = travelJournalBookmarkRepository.getTravelJournalIds(loginUser)
        val journalBookmarkResponses =
            travelJournalBookmarkRepository.getSliceByOther(size, lastId, sortType, user, loginUser).map { bookmark ->
                val profileUrl = fileService.getPreAuthenticatedObjectUrl(bookmark.user.profile.profileName)
                val travelJournalMainImageUrl =
                    fileService.getPreAuthenticatedObjectUrl(
                        bookmark.travelJournal.travelJournalContents[0].travelJournalContentImages[0].contentImage.name,
                    )
                TravelJournalBookmarkSummaryResponse.from(
                    bookmark,
                    profileUrl,
                    travelJournalMainImageUrl,
                    bookmark.travelJournal.user,
                    bookmark.user.id in followingIdList,
                    bookmark.travelJournal.id in bookmarkTravelJournalIdList,
                    false,
                )
            }
        return SliceResponse(size, journalBookmarkResponses)
    }

    @Transactional
    fun deleteTravelJournalBookmark(
        userId: Long,
        travelJournalId: Long,
    ) {
        val user = userRepository.getByUserId(userId)
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        travelJournalBookmarkRepository.deleteByUserAndTravelJournal(user, travelJournal)
    }
}
