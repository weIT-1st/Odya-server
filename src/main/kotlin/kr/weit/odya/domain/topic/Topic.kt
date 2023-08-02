package kr.weit.odya.domain.topic

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "topic_unique",
            columnNames = ["word"],
        ),
    ],
)
@SequenceGenerator(
    name = "TOPIC_SEQ_GENERATOR",
    sequenceName = "TOPIC_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Entity
class Topic(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TOPIC_SEQ_GENERATOR")
    val id: Long,

    @Column(length = 30, nullable = false)
    val word: String,
)
