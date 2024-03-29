package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.agreedTerms.AgreedTerms
import kr.weit.odya.domain.agreedTerms.AgreedTermsRepository
import kr.weit.odya.domain.terms.TermsRepository
import kr.weit.odya.domain.terms.getByRequired
import kr.weit.odya.domain.terms.getByTermsId
import kr.weit.odya.domain.terms.getIdByRequire
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_NOT_EXIST_USER_ID
import kr.weit.odya.support.TEST_OTHER_TERMS_ID
import kr.weit.odya.support.TEST_OTHER_TERMS_ID_2
import kr.weit.odya.support.TEST_REQUIRED_TERMS_TITLE_2
import kr.weit.odya.support.TEST_TERMS_ID
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createAgreedTermsList
import kr.weit.odya.support.createModifyAgreedTermsRequest
import kr.weit.odya.support.createOptionalAgreedTermsList
import kr.weit.odya.support.createOptionalTerms
import kr.weit.odya.support.createOptionalTermsList
import kr.weit.odya.support.createRequiredTerms
import kr.weit.odya.support.createRequiredTermsIdList
import kr.weit.odya.support.createTermsIdList
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

        describe("checkRequiredTerms 메소드") {
            context("필수 약관이 모두 포함된 약관 ID 리스트가 들어올 경우") {
                every { termsRepository.getIdByRequire() } returns createRequiredTermsIdList()
                it("등록된 약관의 id와 제목이 반환된다") {
                    shouldNotThrowAny { termsService.checkRequiredTerms(createTermsIdList()) }
                }
            }

            context("필수 약관이 모두 포함되지 않은 약관 ID 리스트가 들어올 경우") {
                every { termsRepository.getIdByRequire() } returns createRequiredTermsIdList()
                it("[IllegalArgumentException]을 반환된다") {
                    shouldThrow<IllegalArgumentException> { termsService.checkRequiredTerms(setOf(TEST_TERMS_ID)) }
                }
            }
        }

        describe("saveAllAgreedTerms 메소드") {
            context("동의한 약관 ID 리스트가 들어올 경우") {
                every { termsRepository.getByTermsId(TEST_TERMS_ID) } returns createRequiredTerms()
                every { termsRepository.getByTermsId(TEST_OTHER_TERMS_ID) } returns createOptionalTerms()
                every { termsRepository.getByTermsId(TEST_OTHER_TERMS_ID_2) } returns createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2)
                every { agreedTermsRepository.saveAll(any<List<AgreedTerms>>()) } returns createAgreedTermsList()
                it("전부 저장한다") {
                    shouldNotThrowAny { termsService.saveAllAgreedTerms(user, createTermsIdList()) }
                }
            }

            context("존재하지 않는 약관 ID가 포함된 리스트가 들어올 경우") {
                every { termsRepository.getByTermsId(any()) } throws NoSuchElementException()
                it("[NoSuchElementException]을 반환한다") {
                    shouldThrow<NoSuchElementException> { termsService.saveAllAgreedTerms(user, createTermsIdList()) }
                }
            }
        }

        describe("getOptionalTermsListAndOptionalAgreedTerms 메소드") {
            context("유효한 유저 ID가 들어올 경우") {
                every { termsRepository.getByRequired() } returns createOptionalTermsList()
                every { agreedTermsRepository.getAgreedTermsByUserIdAndRequired(TEST_USER_ID, false) } returns createOptionalAgreedTermsList()
                it("선택 약관 리스트와 유저가 동의한 선택 약관 리스트를 반환한다") {
                    shouldNotThrowAny { termsService.getOptionalTermsListAndOptionalAgreedTerms(TEST_USER_ID) }
                }
            }
        }

        describe("modifyAgreedTerms 메소드") {
            context("유효한 동의로 변경한 약관 ID 리스트와 미동의로 변경한 약관 ID 리스트와 유저 ID가 들어올 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { termsRepository.getIdByRequire() } returns createRequiredTermsIdList()
                every { termsRepository.getByTermsId(any()) } returns createRequiredTerms()
                every { agreedTermsRepository.existsByUserIdAndTermsId(TEST_USER_ID, any()) } returns false
                every { agreedTermsRepository.saveAll(any<List<AgreedTerms>>()) } returns createAgreedTermsList()
                every { agreedTermsRepository.deleteAllByUserIdAndTermsIdIn(TEST_USER_ID, any()) } returns Unit
                it("전부 저장 및 삭제한다") {
                    shouldNotThrowAny { termsService.modifyAgreedTerms(createModifyAgreedTermsRequest(), TEST_USER_ID) }
                }
            }

            context("유효하지 않은 유저 ID가 들어올 경우") {
                every { userRepository.getByUserId(TEST_NOT_EXIST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException]을 반환한다") {
                    shouldThrow<NoSuchElementException> { termsService.modifyAgreedTerms(createModifyAgreedTermsRequest(), TEST_NOT_EXIST_USER_ID) }
                }
            }

            context("존재하지 않는 약관 ID가 동의 약관 리스트로 들어올 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { agreedTermsRepository.existsByUserIdAndTermsId(TEST_USER_ID, any()) } returns false
                every { termsRepository.getByTermsId(any()) } throws NoSuchElementException()
                it("[NoSuchElementException]을 반환한다") {
                    shouldThrow<NoSuchElementException> { termsService.modifyAgreedTerms(createModifyAgreedTermsRequest(), TEST_USER_ID) }
                }
            }

            context("필수 약관 ID가 포함된 미동의 약관 리스트와 유저 ID가 들어올 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns user
                every { agreedTermsRepository.existsByUserIdAndTermsId(TEST_USER_ID, any()) } returns true
                every { termsRepository.getIdByRequire() } returns createRequiredTermsIdList()
                it("[IllegalArgumentException]을 반환한다") {
                    shouldThrow<IllegalArgumentException> { termsService.modifyAgreedTerms(createModifyAgreedTermsRequest().copy(disagreeTermsIdList = setOf(TEST_TERMS_ID)), TEST_USER_ID) }
                }
            }
        }
    },
)
