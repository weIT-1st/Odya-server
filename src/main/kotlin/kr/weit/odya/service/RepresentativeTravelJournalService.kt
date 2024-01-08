package kr.weit.odya.service

import jakarta.ws.rs.ForbiddenException
import kr.weit.odya.domain.follow.FollowRepository
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalRepository
import kr.weit.odya.domain.representativetraveljournal.RepresentativeTravelJournalSortType
import kr.weit.odya.domain.representativetraveljournal.getSliceBy
import kr.weit.odya.domain.representativetraveljournal.getTargetSliceBy
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.traveljournal.TravelJournalRepository
import kr.weit.odya.domain.traveljournal.getByTravelJournalId
import kr.weit.odya.domain.traveljournalbookmark.RepresentativeTravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.RepTravelJournalSummaryResponse
import kr.weit.odya.service.dto.SliceResponse
import kr.weit.odya.service.dto.TravelCompanionSimpleResponse
import kr.weit.odya.service.dto.UserSimpleResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RepresentativeTravelJournalService(
    val userRepository: UserRepository,
    val travelJournalRepository: TravelJournalRepository,
    val repTravelJournalRepository: RepresentativeTravelJournalRepository,
    val followRepository: FollowRepository,
    val fileService: FileService,
) {
    @Transactional
    fun createRepTravelJournal(userId: Long, travelJournalId: Long) {
        val user = userRepository.getByUserId(userId)
        val travelJournal = travelJournalRepository.getByTravelJournalId(travelJournalId)
        validateTravelJournalCreation(travelJournal, userId, user)

        repTravelJournalRepository.save(
            RepresentativeTravelJournal(
                user = user,
                travelJournal = travelJournal,
            ),
        )
    }

    @Transactional(readOnly = true)
    fun getTargetRepTravelJournals(
        loginUserId: Long,
        targetUserId: Long,
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
    ): SliceResponse<RepTravelJournalSummaryResponse> {
        val targetUser = userRepository.getByUserId(targetUserId)
        val isFollowing = followRepository.existsByFollowerIdAndFollowingId(loginUserId, targetUserId)
        val repTravelJournalSummaryResponses =
            repTravelJournalRepository.getTargetSliceBy(size, lastId, sortType, targetUser, loginUserId)
                .map { repTravelJournal ->
                    val profileUrl = fileService.getPreAuthenticatedObjectUrl(repTravelJournal.user.profile.profileName)
                    val travelJournalMainImageUrl =
                        fileService.getPreAuthenticatedObjectUrl(repTravelJournal.travelJournal.travelJournalContents[0].travelJournalContentImages[0].contentImage.name)
                    RepTravelJournalSummaryResponse.from(
                        repTravelJournal,
                        travelJournalMainImageUrl,
                        UserSimpleResponse(
                            repTravelJournal.user,
                            profileUrl,
                            isFollowing,
                        ),
                        getTravelCompanionSimpleResponses(repTravelJournal.travelJournal),
                    )
                }
        return SliceResponse(size, repTravelJournalSummaryResponses)
    }

    @Transactional(readOnly = true)
    fun getMyRepTravelJournals(
        userId: Long,
        size: Int,
        lastId: Long?,
        sortType: RepresentativeTravelJournalSortType,
    ): SliceResponse<RepTravelJournalSummaryResponse> {
        val user = userRepository.getByUserId(userId)
        val repTravelJournalSummaryResponses =
            repTravelJournalRepository.getSliceBy(size, lastId, sortType, user).map { repTravelJournal ->
                val profileUrl = fileService.getPreAuthenticatedObjectUrl(repTravelJournal.user.profile.profileName)
                val travelJournalMainImageUrl =
                    fileService.getPreAuthenticatedObjectUrl(repTravelJournal.travelJournal.travelJournalContents[0].travelJournalContentImages[0].contentImage.name)
                RepTravelJournalSummaryResponse.from(
                    repTravelJournal,
                    travelJournalMainImageUrl,
                    UserSimpleResponse(
                        repTravelJournal.user,
                        profileUrl,
                        false,
                    ),
                    getTravelCompanionSimpleResponses(repTravelJournal.travelJournal),
                )
            }
        return SliceResponse(size, repTravelJournalSummaryResponses)
    }

    @Transactional
    fun deleteRepTravelJournal(userId: Long, repTravelJournalId: Long) {
        val user = userRepository.getByUserId(userId)
        val travelJournal = travelJournalRepository.getByTravelJournalId(repTravelJournalId)

        if (travelJournal.user.id != userId) {
            throw ForbiddenException("대표 여행일지($repTravelJournalId)는 자신의 것만 삭제할 수 있습니다.")
        }

        repTravelJournalRepository.deleteByUserAndTravelJournal(user, travelJournal)
    }

    private fun validateTravelJournalCreation(
        travelJournal: TravelJournal,
        userId: Long,
        user: User,
    ) {
        if (travelJournal.user.id != userId) {
            throw ForbiddenException("대표 여행일지로 등록할 수 있는 여행일지(${travelJournal.id})는 자신의 것만 가능합니다.")
        }

        if (repTravelJournalRepository.existsByUserAndTravelJournal(user, travelJournal)) {
            throw ExistResourceException("이미 사용자(${user.id})가 대표 여행일지(${travelJournal.id})로 등록하였습니다.")
        }
    }

    private fun getTravelCompanionSimpleResponses(it: TravelJournal): List<TravelCompanionSimpleResponse> =
        it.travelCompanions
            .map { travelCompanion ->
                if (travelCompanion.user != null) {
                    TravelCompanionSimpleResponse(
                        travelCompanion.user!!.username,
                        fileService.getPreAuthenticatedObjectUrl(it.user.profile.profileName),
                    )
                } else {
                    TravelCompanionSimpleResponse(travelCompanion.username, null)
                }
            }
}
