package kr.weit.odya.domain.profilecolor

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun ProfileColorRepository.getProfileColorById(profileColorId: Long): ProfileColor =
    findByIdOrNull(profileColorId) ?: throw NoSuchElementException("프로필 색상이 존재하지 않습니다")

interface ProfileColorRepository : JpaRepository<ProfileColor, Long>
