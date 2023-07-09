package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.profilecolor.ProfileColorRepository
import kr.weit.odya.domain.profilecolor.getProfileColorById
import kr.weit.odya.service.generator.ProfileColorRandomIndexGenerator
import kr.weit.odya.support.NOT_EXIST_NONE_PROFILE_COLOR_ERROR_MESSAGE
import kr.weit.odya.support.TEST_PROFILE_COLOR_ID
import kr.weit.odya.support.createEmptyProfileColorList
import kr.weit.odya.support.createProfileColor
import kr.weit.odya.support.createProfileColorList

class ProfileColorServiceTest : DescribeSpec(
    {
        val profileColorRepository = mockk<ProfileColorRepository>()
        val profileColorRandomIndexGenerator = mockk<ProfileColorRandomIndexGenerator>()
        val profileColorService = ProfileColorService(profileColorRepository, profileColorRandomIndexGenerator)

        describe("getRandomProfileColor") {
            context("프로필 색상이 정상적으로 존재할 경우") {
                val profileColorList = createProfileColorList()
                every { profileColorRepository.findAll() } returns profileColorList
                every { profileColorRandomIndexGenerator.getRandomNumber(profileColorList.size) } returns 0
                it("랜덤 프로필 색상이 반환된다") {
                    val result = profileColorService.getRandomProfileColor()
                    result.id shouldBe profileColorList[0].id
                }
            }

            context("DB에 프로필 색상이 존재하지 않을 경우") {
                every { profileColorRepository.findAll() } returns createEmptyProfileColorList()
                it("[NotFoundDefaultResourceException] 반환한다") {
                    shouldThrow<NotFoundDefaultResourceException> { profileColorService.getRandomProfileColor() }
                }
            }
        }

        describe("getNoneProfileColor") {
            context("NONE 프로필 색상이 정상적으로 존재할 경우") {
                every { profileColorRepository.getProfileColorById(TEST_PROFILE_COLOR_ID) } returns createProfileColor(
                    TEST_PROFILE_COLOR_ID,
                )
                it("NONE 프로필 색상이 반환된다") {
                    val result = profileColorService.getNoneProfileColor()
                    result.id shouldBe TEST_PROFILE_COLOR_ID
                }
            }

            context("NONE 프로필 색상이 존재하지 않을 경우") {
                every { profileColorRepository.getProfileColorById(TEST_PROFILE_COLOR_ID) } throws NotFoundDefaultResourceException(
                    NOT_EXIST_NONE_PROFILE_COLOR_ERROR_MESSAGE,
                )
                it("[NotFoundDefaultResourceException] 반환한다") {
                    shouldThrow<NotFoundDefaultResourceException> { profileColorService.getNoneProfileColor() }
                }
            }
        }
    },
)
