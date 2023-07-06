package kr.weit.odya.domain.profilecolor

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import kr.weit.odya.support.domain.BaseModifiableEntity

const val NONE_PROFILE_COLOR_HEX = "NONE"

@Entity
@SequenceGenerator(
    name = "PROFILE_COLOR_SEQ_GENERATOR",
    sequenceName = "PROFILE_COLOR_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class ProfileColor(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PROFILE_COLOR_SEQ_GENERATOR")
    val id: Long = 0L,

    @Column(nullable = false, updatable = false, length = 7)
    val colorHex: String,

    @Column(nullable = false, updatable = false)
    val red: Int,

    @Column(nullable = false, updatable = false)
    val green: Int,

    @Column(nullable = false, updatable = false)
    val blue: Int
) : BaseModifiableEntity()
