package kr.weit.odya.domain.terms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

fun TermsRepository.getByTermsId(termsId: Long): Terms {
    return findByIdOrNull(termsId) ?: throw NoSuchElementException("$termsId : 해당 약관이 존재하지 않습니다.")
}

fun TermsRepository.getByRequired(required: Boolean = false): List<Terms> {
    return findAllByRequired(required)
}

fun TermsRepository.getIdByRequire(required: Boolean = true): List<Long> {
    return findIdByRequired(required)
}

@Repository
interface TermsRepository : JpaRepository<Terms, Long> {
    fun findAllByRequired(required: Boolean): List<Terms>

    @Query("SELECT t.id FROM Terms t WHERE t.required = :required")
    fun findIdByRequired(required: Boolean): List<Long>
}
