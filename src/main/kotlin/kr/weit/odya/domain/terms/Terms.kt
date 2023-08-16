package kr.weit.odya.domain.terms

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.support.domain.BaseModifiableEntity

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "terms_title_unique",
            columnNames = ["title"],
        ),
    ],
)
@SequenceGenerator(
    name = "TERMS_SEQ_GENERATOR",
    sequenceName = "TERMS_TOPIC_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Entity
class Terms(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TERMS_SEQ_GENERATOR")
    val id: Long,

    @Column(nullable = false, length = 60)
    val title: String,

    @Column(columnDefinition = "CLOB NOT NULL")
    @Lob
    val content: String,

    @Column(columnDefinition = "NUMERIC(1) NOT NULL DEFAULT 0")
    val required: Int,
) : BaseModifiableEntity()
