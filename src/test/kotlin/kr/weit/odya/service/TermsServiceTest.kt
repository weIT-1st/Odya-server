package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.agreedTerms.AgreedTermsRepository
import kr.weit.odya.domain.terms.TermsRepository
import kr.weit.odya.domain.terms.getByTermsId
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_TERMS_ID
import kr.weit.odya.support.createRequiredTerms
import kr.weit.odya.support.createTermsList
import kr.weit.odya.support.createUser

class TermsServiceTest : DescribeSpec(
    {
        val termsRepository = mockk<TermsRepository>()
        val agreedTermsRepository = mockk<AgreedTermsRepository>()
        val userRepository = mockk<UserRepository>()
        val termsService = TermsService(termsRepository, agreedTermsRepository, userRepository)
        val user = createUser()

        describe("getTermsList 메소드") {
            context("약관이 정상적으로 있을 경우") {
                every { termsRepository.findAll() } returns createTermsList()
                it("등록된 약관의 id와 제목이 반환된다") {
                    shouldNotThrowAny { termsService.getTermsList() }
                }
            }
        }

        describe("getTermsContent 메소드") {
            context("약관 ID가 들어올 경우") {
                every { termsRepository.getByTermsId(TEST_TERMS_ID) } returns createRequiredTerms()
                it("등록된 약관의 id와 제목이 반환된다") {
                    shouldNotThrowAny { termsService.getTermsContent(TEST_TERMS_ID) }
                }
            }

            context("존재하지 않는 약관 ID가 들어올 경우") {
                every { termsRepository.getByTermsId(TEST_TERMS_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 반환된다") {
                    shouldThrow<NoSuchElementException> { termsService.getTermsContent(TEST_TERMS_ID) }
                }
            }
        }
    },
)
