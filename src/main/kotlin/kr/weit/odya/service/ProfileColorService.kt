package kr.weit.odya.service

import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.domain.profilecolor.ProfileColorRepository
import kr.weit.odya.domain.profilecolor.getProfileColorById
import kr.weit.odya.service.generator.ProfileColorRandomIndexGenerator
import kr.weit.odya.util.getOrThrow
import org.springframework.stereotype.Service

@Service
class ProfileColorService(
    private val profileColorRepository: ProfileColorRepository,
    private val profileColorRandomIndexGenerator: ProfileColorRandomIndexGenerator,
) {
    private val noneProfileColorPk = 1L

    fun getRandomProfileColor(): ProfileColor {
        val profileColors = profileColorRepository.findAll().toList()
        if (profileColors.isEmpty()) {
            throw NotFoundDefaultResourceException("프로필 색상이 존재하지 않습니다")
        }
        return profileColors[profileColorRandomIndexGenerator.getRandomNumber(profileColors.size)]
    }

    fun getNoneProfileColor(): ProfileColor {
        return runCatching { profileColorRepository.getProfileColorById(noneProfileColorPk) }
            .getOrThrow { throw NotFoundDefaultResourceException("프로필 색상(NONE)이 존재하지 않습니다") }
    }
}
