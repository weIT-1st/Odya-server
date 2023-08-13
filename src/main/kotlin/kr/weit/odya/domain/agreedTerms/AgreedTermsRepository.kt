package kr.weit.odya.domain.agreedTerms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AgreedTermsRepository : JpaRepository<AgreedTerms, Long>
