package kr.weit.odya.domain.profilecolor

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.support.TEST_NONE_COLOR_HEX
import kr.weit.odya.support.TEST_PROFILE_COLOR_ID
import kr.weit.odya.support.createProfileColor
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class ProfileColorRepositoryTest(
    private val profileColorRepository: ProfileColorRepository
) : ExpectSpec({
    beforeEach {
        profileColorRepository.save(createProfileColor())
    }

    context("프로필 색상 조회") {
        expect("PROFILE_COLOR_ID가 일치하는 프로필 색상을 조회한다") {
            val result = profileColorRepository.getProfileColorById(TEST_PROFILE_COLOR_ID)
            result.colorHex shouldBe TEST_NONE_COLOR_HEX
        }
    }
},)
