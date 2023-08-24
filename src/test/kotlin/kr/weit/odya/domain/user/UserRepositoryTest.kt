package kr.weit.odya.domain.user

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.support.TEST_DEFAULT_PROFILE_PNG
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_OTHER_USER_ID
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createOtherUser
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class UserRepositoryTest(
    private val userRepository: UserRepository,
) : ExpectSpec(
    {
        beforeEach {
            userRepository.save(createUser())
            userRepository.save(createOtherUser())
        }

        context("사용자 조회") {
            expect("USERNAME이 일치하는 사용자를 조회한다") {
                val result = userRepository.getByUsername(TEST_USERNAME)
                result.username shouldBe TEST_USERNAME
            }

            expect("USER_ID가 일치하는 사용자를 조회한다") {
                val result = userRepository.getByUserId(TEST_USER_ID)
                result.username shouldBe TEST_USERNAME
            }

            expect("USER_ID가 일치하는 사용자를 프로필과 함께 조회한다") {
                val result = userRepository.getByUserIdWithProfile(TEST_USER_ID)
                result.profile.profileName shouldBe TEST_DEFAULT_PROFILE_PNG
            }
        }

        context("사용자 목록 조회") {
            expect("사용자 ID 목록에 해당되는 모든 사용자를 조회한다") {
                val userIds = listOf(TEST_USER_ID, TEST_OTHER_USER_ID)
                val result = userRepository.getByUserIds(userIds)
                result.size shouldBe 2
            }
        }

        context("사용자 여부 확인") {
            expect("USERNAME이 일치하는 사용자 여부를 확인한다") {
                val result = userRepository.existsByUsername(TEST_USERNAME)
                result shouldBe true
            }

            expect("닉네임이 일치하는 사용자 여부를 확인한다") {
                val result = userRepository.existsByNickname(TEST_NICKNAME)
                result shouldBe true
            }

            expect("이메일이 일치하는 사용자 여부를 확인한다") {
                val result = userRepository.existsByEmail(TEST_EMAIL)
                result shouldBe true
            }

            expect("전화번호가 일치하는 사용자 여부를 확인한다") {
                val result = userRepository.existsByPhoneNumber(TEST_PHONE_NUMBER)
                result shouldBe true
            }

            expect("사용자 ID 목록에 해당하는 모든 사용자 여부를 확인한다") {
                val userIds = listOf(TEST_USER_ID, TEST_OTHER_USER_ID)
                val result = userRepository.existsAllByUserIds(userIds, userIds.size)
                result shouldBe true
            }
        }
    },
)
