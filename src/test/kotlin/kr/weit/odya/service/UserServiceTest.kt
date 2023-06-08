package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createUser
import kr.weit.odya.support.createUserResponse

class UserServiceTest : DescribeSpec({
    val userRepository = mockk<UserRepository>()

    val userService = UserService(userRepository)

    describe("getInformation") {
        context("가입되어 있는 USERNAME이 주어지는 경우") {
            every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
            it("UserResponse를 반환한다") {
                val userResponse = userService.getInformation(TEST_USER_ID)
                userResponse shouldBe createUserResponse()
            }
        }

        context("가입되어 있지 않은 USERNAME이 주어지는 경우") {
            every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
            it("[NoSuchElementException] 예외가 발생한다") {
                shouldThrow<NoSuchElementException> { userService.getInformation(TEST_USER_ID) }
            }
        }
    }
})
