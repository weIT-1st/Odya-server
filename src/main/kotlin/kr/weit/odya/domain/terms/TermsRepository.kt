package kr.weit.odya.domain.terms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun TermsRepository.getByTermsId(termsId: Long): Terms {
    return findByIdOrNull(termsId) ?: throw NoSuchElementException("$termsId : 해당 약관이 존재하지 않습니다.")
}

fun TermsRepository.getRequiredTerms(required: Boolean = true): List<Terms> {
    return findByRequired(required)
}

@Repository
interface TermsRepository : JpaRepository<Terms, Long> {
    fun findByRequired(required: Boolean): List<Terms>
}
