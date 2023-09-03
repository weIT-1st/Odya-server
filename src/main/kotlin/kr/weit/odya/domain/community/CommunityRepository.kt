package kr.weit.odya.domain.community

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun CommunityRepository.getByCommunityId(communityId: Long) = findByIdOrNull(communityId) ?: throw NoSuchElementException("$communityId : 존재하지 않는 커뮤니티 글입니다.")

@Repository
interface CommunityRepository : JpaRepository<Community, Long>
