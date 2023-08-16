package kr.weit.odya.domain.agreedTerms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AgreedTermsRepository : JpaRepository<AgreedTerms, Long> {
    @Query("SELECT a FROM AgreedTerms a WHERE a.user.id = :userId and a.terms.required = :required")
    fun getAgreedTermsByUserIdAndRequired(userId: Long, required: Int = 0): List<AgreedTerms>

    fun findByTermsIdAndUserId(termsId: Long, userId: Long): AgreedTerms?

    fun deleteAllByUserIdAndTermsIdIn(userId: Long, termsId: Set<Long>?): List<AgreedTerms>?

    fun existsByUserIdAndTermsId(userId: Long, termsId: Long): Boolean
}
