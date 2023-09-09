package kr.weit.odya.domain.community

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun CommunityRepository.getByCommunityId(communityId: Long): Community =
    findByIdOrNull(communityId) ?: throw IllegalArgumentException("$communityId: 존재하지 않는 커뮤니티입니다.")

@Repository
interface CommunityRepository : JpaRepository<Community, Long>
