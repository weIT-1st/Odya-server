package kr.weit.odya.service

import jakarta.transaction.Transactional
import kr.weit.odya.domain.agreedTerms.AgreedTerms
import kr.weit.odya.domain.agreedTerms.AgreedTermsRepository
import kr.weit.odya.domain.terms.TermsRepository
import kr.weit.odya.domain.terms.getByTermsId
import kr.weit.odya.domain.terms.getRequiredTerms
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.service.dto.TermsContentResponse
import kr.weit.odya.service.dto.TermsTitleListResponse
import org.springframework.stereotype.Service

@Service
class TermsService(
    private val termsRepository: TermsRepository,
    private val agreedTermsRepository: AgreedTermsRepository,
    private val userRepository: UserRepository,
) {
    fun getTermsList(): List<TermsTitleListResponse> {
        return termsRepository.findAll().map { TermsTitleListResponse(it) }
    }

    fun getTermsContent(termsId: Long): TermsContentResponse {
        return TermsContentResponse(
            termsRepository.getByTermsId(termsId),
        )
    }

    @Transactional
    fun checkRequiredTerms(termsIdList: List<Long>) {
        val requiredTermsList = termsRepository.getRequiredTerms().map { it.id }
        termsIdList.containsAll(requiredTermsList).also { contain ->
            if (!contain) {
                requiredTermsList.forEach { termsId ->
                    if (!termsIdList.contains(termsId)) {
                        throw NoSuchElementException("$termsId : 필수 약관에 동의하지 않았습니다.")
                    }
                }
            }
        }
    }

    @Transactional
    fun saveAllAgreedTerms(user: User, termsIdList: List<Long>) {
        val agreedTermsList: List<AgreedTerms> = termsIdList.map { AgreedTerms(0L, user, termsRepository.getByTermsId(it)) }
        agreedTermsRepository.saveAll(agreedTermsList)
    }

    fun getUpdateTermsList(): List<TermsTitleListResponse> {
        return termsRepository.findByRequired(false).map { TermsTitleListResponse(it) }
    }
}
