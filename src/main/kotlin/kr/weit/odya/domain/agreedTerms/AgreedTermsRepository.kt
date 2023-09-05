package kr.weit.odya.domain.agreedTerms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface AgreedTermsRepository : JpaRepository<AgreedTerms, Long> {
    @Query("SELECT a FROM AgreedTerms a WHERE a.user.id = :userId and a.terms.required = :required")
    fun getAgreedTermsByUserIdAndRequired(userId: Long, required: Boolean = false): List<AgreedTerms>

    fun deleteAllByUserIdAndTermsIdIn(userId: Long, termsId: Set<Long>?)

    fun existsByUserIdAndTermsId(userId: Long, termsId: Long): Boolean

    fun deleteAllByUserId(userId: Long)
}
