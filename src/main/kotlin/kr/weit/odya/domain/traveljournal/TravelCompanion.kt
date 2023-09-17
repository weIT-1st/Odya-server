package kr.weit.odya.domain.traveljournal

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
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    indexes = [
        Index(name = "travel_journal_id_index", columnList = "travel_journal_id"),
        Index(name = "user_id_index", columnList = "user_id"),
    ],
)
@Entity
@SequenceGenerator(
    name = "TRAVEL_COMPANION_SEQ_GENERATOR",
    sequenceName = "TRAVEL_COMPANION_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class TravelCompanion(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TRAVEL_COMPANION_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "user_id", updatable = false)
    val user: User?,

    @Column
    val username: String?,
) : BaseTimeEntity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other as? TravelCompanion == null) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
