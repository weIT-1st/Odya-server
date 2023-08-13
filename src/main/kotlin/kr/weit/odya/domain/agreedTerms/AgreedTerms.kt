package kr.weit.odya.domain.agreedTerms

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Entity
@SequenceGenerator(
    name = "AGREED_TERMS_SEQ_GENERATOR",
    sequenceName = "AGREED_TERMS_TOPIC_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class AgreedTerms(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AGREED_TERMS_SEQ_GENERATOR")
    val id: Long,

    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0) NOT NULL")
    @ManyToOne
    val user: User,

    @JoinColumn(name = "terms_id", columnDefinition = "NUMERIC(19, 0) NOT NULL")
    @ManyToOne
    val terms: Terms,
) : BaseTimeEntity()
