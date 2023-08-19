package kr.weit.odya.domain.terms

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.support.TEST_OPTIONAL_TERMS_TITLE
import kr.weit.odya.support.TEST_OTHER_TERMS_ID_2
import kr.weit.odya.support.TEST_REQUIRED_TERMS_TITLE
import kr.weit.odya.support.TEST_REQUIRED_TERMS_TITLE_2
import kr.weit.odya.support.createOptionalTerms
import kr.weit.odya.support.createRequiredTerms
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TermsRepositoryTest(private val termsRepository: TermsRepository) : ExpectSpec(
    {
        lateinit var requiredTerms: Terms
        lateinit var requiredTerms2: Terms
        lateinit var optionalTerms: Terms
        beforeEach {
            requiredTerms = termsRepository.save(createRequiredTerms())
            optionalTerms = termsRepository.save(createOptionalTerms())
            requiredTerms2 = termsRepository.save(createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2))
        }

        context("약관 조회") {
            expect("TERMS_ID가 일치하는 약관을 조회한다") {
                val result = termsRepository.getByTermsId(requiredTerms.id)
                result.title shouldBe TEST_REQUIRED_TERMS_TITLE
            }
        }

        context("필수/선택 약관 모두 조회") {
            expect("필수 약관을 모두 조회한다") {
                val result = termsRepository.getRequiredTerms()
                result.map { it.title } shouldBe listOf(TEST_REQUIRED_TERMS_TITLE, TEST_REQUIRED_TERMS_TITLE_2)
            }

            expect("선택 약관을 모두 조회한다") {
                val result = termsRepository.getRequiredTerms(0)
                result[0].title shouldBe TEST_OPTIONAL_TERMS_TITLE
            }
        }
    },
)
