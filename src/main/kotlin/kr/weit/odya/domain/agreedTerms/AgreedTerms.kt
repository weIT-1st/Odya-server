package kr.weit.odya.domain.agreedTerms

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "agreed_terms_unique",
            columnNames = ["user_id", "terms_id"],
        ),
    ],
    indexes = [
        Index(name = "agreed_terms_terms_id_index", columnList = "terms_id"),
    ],
)
@SequenceGenerator(
    name = "AGREED_TERMS_SEQ_GENERATOR",
    sequenceName = "AGREED_TERMS_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Entity
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
