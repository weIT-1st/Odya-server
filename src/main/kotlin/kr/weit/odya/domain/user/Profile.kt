package kr.weit.odya.domain.user

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
import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.support.domain.BaseModifiableEntity

const val DEFAULT_PROFILE_PNG = "default_profile.png"

@Table(
    indexes = [
        Index(name = "profile_profile_color_id_index", columnList = "profile_color_id"),
    ],
)
@Entity
@SequenceGenerator(name = "PROFILE_SEQ_GENERATOR", sequenceName = "PROFILE_SEQ", initialValue = 1, allocationSize = 1)
class Profile(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PROFILE_SEQ_GENERATOR")
    val id: Long = 0L,

    profileName: String = DEFAULT_PROFILE_PNG,

    originFileName: String = DEFAULT_PROFILE_PNG,

    profileColor: ProfileColor,
) : BaseModifiableEntity() {
    @Column(nullable = false, length = 30)
    var profileName: String = profileName
        protected set

    @Column(nullable = false)
    var originFileName: String = originFileName
        protected set

    @ManyToOne
    @JoinColumn(name = "profile_color_id", nullable = false)
    var profileColor: ProfileColor = profileColor
        protected set

    fun changeProfile(
        profileName: String,
        originFileName: String,
        profileColor: ProfileColor,
    ) {
        this.profileName = profileName
        this.originFileName = originFileName
        this.profileColor = profileColor
    }
}
