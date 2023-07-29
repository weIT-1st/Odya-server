package kr.weit.odya.domain.topic

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Topic(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TOPIC_SEQ_GENERATOR")
    val id: Long,
    @Column(nullable = false)
    val word: String,
)
