package kr.weit.odya.service

import kr.weit.odya.domain.community.CommunityRepository
import kr.weit.odya.domain.community.getByCommunityId
import kr.weit.odya.domain.communitylike.CommunityLike
import kr.weit.odya.domain.communitylike.CommunityLikeId
import kr.weit.odya.domain.communitylike.CommunityLikeRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommunityLikeService(
    private val communityLikeRepository: CommunityLikeRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun increaseCommunityLikeCount(communityId: Long, userId: Long) {
        val community = communityRepository.getByCommunityId(communityId)
        val user = userRepository.getByUserId(userId)

        if (communityLikeRepository.existsById(CommunityLikeId(community, user))) {
            throw ExistResourceException("요청 사용자($userId)가 이미 좋아요를 누른 게시글($communityId)입니다.")
        }
        community.increaseLikeCount()
        communityLikeRepository.save(CommunityLike(community, user))
    }

    @Transactional
    fun decreaseCommunityLikeCount(communityId: Long, userId: Long) {
        val community = communityRepository.getByCommunityId(communityId)
        val user = userRepository.getByUserId(userId)

        community.decreaseLikeCount()
        communityLikeRepository.deleteById(CommunityLikeId(community, user))
    }
}
