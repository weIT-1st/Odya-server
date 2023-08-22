package kr.weit.odya.service

import jakarta.transaction.Transactional
import kr.weit.odya.domain.agreedTerms.AgreedTerms
import kr.weit.odya.domain.agreedTerms.AgreedTermsRepository
import kr.weit.odya.domain.terms.TermsRepository
import kr.weit.odya.domain.terms.getByTermsId
import kr.weit.odya.domain.terms.getRequiredTerms
import kr.weit.odya.domain.user.User
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.service.dto.ModifyAgreedTermsRequest
import kr.weit.odya.service.dto.OptionalAgreedTermsResponse
import kr.weit.odya.service.dto.TermsContentResponse
import kr.weit.odya.service.dto.TermsTitleListResponse
import kr.weit.odya.service.dto.TermsUpdateResponse
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
    fun checkRequiredTerms(termsIdList: Set<Long>) {
        val requiredTermsList = termsRepository.getRequiredTerms().map { it.id }
        termsIdList.containsAll(requiredTermsList).also { contain ->
            if (!contain) {
                requiredTermsList.forEach { termsId ->
                    if (!termsIdList.contains(termsId)) {
                        throw IllegalArgumentException("$termsId : 필수 약관에 동의하지 않았습니다.")
                    }
                }
            }
        }
    }

    @Transactional
    fun saveAllAgreedTerms(user: User, termsIdList: Set<Long>) {
        val agreedTermsList: List<AgreedTerms> = termsIdList.map { AgreedTerms(0L, user, termsRepository.getByTermsId(it)) }
        agreedTermsRepository.saveAll(agreedTermsList)
    }

    @Transactional
    fun getOptionalTermsListAndOptionalAgreedTerms(userId: Long): TermsUpdateResponse {
        return TermsUpdateResponse(
            termsRepository.findAllByRequired(0).map { TermsTitleListResponse(it) },
            agreedTermsRepository.getAgreedTermsByUserIdAndRequired(userId, 0).map { OptionalAgreedTermsResponse(it) },
        )
    }

    @Transactional
    fun modifyAgreedTerms(modifyAgreedTermsRequest: ModifyAgreedTermsRequest, userId: Long) {
        val user = userRepository.getByUserId(userId)
        modifyAgreedTermsRequest.agreedTermsIdList?.let { list ->
            val agreedTermsList = list.mapNotNull {
                if (!agreedTermsRepository.existsByUserIdAndTermsId(userId, it)) {
                    AgreedTerms(0L, user, termsRepository.getByTermsId(it))
                } else {
                    null
                }
            }
            agreedTermsRepository.saveAll(agreedTermsList)
        }
        val requiredTermsList = termsRepository.getRequiredTerms().map { it.id }
        modifyAgreedTermsRequest.disagreeTermsIdList?.forEach { termsId ->
            if (requiredTermsList.contains(termsId)) {
                throw IllegalArgumentException("$termsId : 필수 약관은 삭제할 수 없습니다.")
            }
        }
        agreedTermsRepository.deleteAllByUserIdAndTermsIdIn(userId, modifyAgreedTermsRequest.disagreeTermsIdList)
    }
}
