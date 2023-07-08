package kr.weit.odya.service

import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.domain.profilecolor.ProfileColorRepository
import kr.weit.odya.domain.profilecolor.getProfileColorById
import kr.weit.odya.service.generator.ProfileColorRandomIndexGenerator
import org.springframework.stereotype.Service

const val NONE_PROFILE_COLOR_PK = 1L

@Service
class ProfileColorService(
    private val profileColorRepository: ProfileColorRepository,
    private val profileColorRandomIndexGenerator: ProfileColorRandomIndexGenerator,
) {
    fun getRandomProfileColor(): ProfileColor {
        val profileColors = profileColorRepository.findAll().toList()
        if (profileColors.isEmpty()) {
            throw NotFoundDefaultResourceException("프로필 색상이 존재하지 않습니다")
        }
        return profileColors[profileColorRandomIndexGenerator.getRandomNumber(profileColors.size)]
    }

    fun getNoneProfileColor(): ProfileColor {
        return runCatching { profileColorRepository.getProfileColorById(NONE_PROFILE_COLOR_PK) }
            .onFailure { throw NotFoundDefaultResourceException("프로필 색상(NONE)이 존재하지 않습니다") }
            .getOrThrow()
    }
}
