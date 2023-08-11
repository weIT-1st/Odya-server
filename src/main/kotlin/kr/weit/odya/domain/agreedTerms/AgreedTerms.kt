package kr.weit.odya.domain.agreedTerms

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Entity
class AgreedTerms(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AGREED_TERMS_SEQ_GENERATOR")
    val id: Long,

    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "NUMERIC(19, 0)")
    @ManyToOne
    val user: User,

    @JoinColumn(name = "terms_id", nullable = false, columnDefinition = "NUMERIC(19, 0)")
    @ManyToOne
    val terms: Terms,
) : BaseTimeEntity()
