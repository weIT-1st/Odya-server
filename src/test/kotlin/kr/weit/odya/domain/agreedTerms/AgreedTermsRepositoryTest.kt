package kr.weit.odya.domain.agreedTerms

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.support.TEST_NOT_EXIST_TERMS_ID
import kr.weit.odya.support.TEST_OTHER_AGREED_TERMS_ID
import kr.weit.odya.support.TEST_OTHER_AGREED_TERMS_ID_2
import kr.weit.odya.support.TEST_OTHER_TERMS_ID_2
import kr.weit.odya.support.TEST_REQUIRED_TERMS_TITLE_2
import kr.weit.odya.support.TEST_TERMS_ID
import kr.weit.odya.support.createAgreedTerms
import kr.weit.odya.support.createOptionalTerms
import kr.weit.odya.support.createRequiredTerms
import kr.weit.odya.support.createUser
import kr.weit.odya.support.test.BaseTests.RepositoryTest

@RepositoryTest
class AgreedTermsRepositoryTest(private val agreedTermsRepository: AgreedTermsRepository, private val userRepository: UserRepository) : ExpectSpec(
    {
        lateinit var requiredAgreedTerms: AgreedTerms
        lateinit var requiredAgreedTerms2: AgreedTerms
        lateinit var optionalAgreedTerms: AgreedTerms
        lateinit var user: User
        beforeTest {
            user = userRepository.save(createUser())
            requiredAgreedTerms = agreedTermsRepository.save(createAgreedTerms(user))
            optionalAgreedTerms = agreedTermsRepository.save(createAgreedTerms(user, TEST_OTHER_AGREED_TERMS_ID, createOptionalTerms()))
            requiredAgreedTerms2 = agreedTermsRepository.save(createAgreedTerms(user, TEST_OTHER_AGREED_TERMS_ID_2, createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2)))
        }

        context("유저가 동의한 약관 전체 조회") {
            expect("user와 일치하는 선택 약관 전체를 조회한다") {
                val result = agreedTermsRepository.getAgreedTermsByUserIdAndRequired(user.id)
                result.size shouldBe 1
                result[0].terms.required shouldBe false
            }

            expect("user와 일치하는 필수 약관 전체를 조회한다") {
                val result = agreedTermsRepository.getAgreedTermsByUserIdAndRequired(user.id, true)
                result.size shouldBe 2
                result[0].terms.required shouldBe true
            }
        }

        context("유저가 동의한 약관 삭제") {
            expect("userId와 termsId리스트와 일치하는 동의한 약관 모두 삭제한다") {
                agreedTermsRepository.deleteAllByUserIdAndTermsIdIn(user.id, setOf(optionalAgreedTerms.id, requiredAgreedTerms.id))
                val result = agreedTermsRepository.existsByUserIdAndTermsId(user.id, optionalAgreedTerms.id)
                val result2 = agreedTermsRepository.existsByUserIdAndTermsId(user.id, requiredAgreedTerms.id)
                result shouldBe false
                result2 shouldBe false
            }
        }

        context("동의한 약관 존재 여부 조회") {
            expect("userId와 termsId와 일치하는 동의한 약관 여부를 조회한다(존재함)") {
                val result = agreedTermsRepository.existsByUserIdAndTermsId(user.id, TEST_TERMS_ID)
                result shouldBe true
            }

            expect("userId와 termsId와 일치하는 동의한 약관 여부를 조회한다(존재하지 않음)") {
                val result = agreedTermsRepository.existsByUserIdAndTermsId(user.id, TEST_NOT_EXIST_TERMS_ID)
                result shouldBe false
            }
        }
    },
)
