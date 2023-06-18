package kr.weit.odya.domain.user

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import kr.weit.odya.support.TEST_EMAIL
import kr.weit.odya.support.TEST_NICKNAME
import kr.weit.odya.support.TEST_PHONE_NUMBER
import kr.weit.odya.support.TEST_USERNAME
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class UserRepositoryTest(
    private val userRepository: UserRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("사용자 조회") {
        val user = userRepository.save(createUser())

        expect("USERNAME이 일치하는 사용자를 조회한다") {
            val result = userRepository.getByUsername(TEST_USERNAME)
            result.username shouldBe TEST_USERNAME
        }

        expect("USER_ID가 일치하는 사용자를 조회한다") {
            val result = userRepository.getByUserId(user.id)
            result.username shouldBe TEST_USERNAME
        }
    }

    context("사용자 여부 확인") {
        userRepository.save(createUser())

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
    }
})
