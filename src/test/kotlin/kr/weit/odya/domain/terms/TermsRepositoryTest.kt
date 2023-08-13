package kr.weit.odya.domain.terms

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.support.TEST_REQUIRED_TERMS_TITLE
import kr.weit.odya.support.createOptionalTerms
import kr.weit.odya.support.createRequiredTerms
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class TermsRepositoryTest(private val termsRepository: TermsRepository) : ExpectSpec(
    {
        lateinit var requiredTerms: Terms
        lateinit var optionalTerms: Terms
        beforeEach {
            requiredTerms = termsRepository.save(createRequiredTerms())
            optionalTerms = termsRepository.save(createOptionalTerms())
        }

        context("약관 조회") {
            expect("TERMS_ID가 일치하는 약관을 조회한다") {
                val result = termsRepository.getByTermsId(requiredTerms.id)
                result.title shouldBe TEST_REQUIRED_TERMS_TITLE
            }
        }
    },
)
