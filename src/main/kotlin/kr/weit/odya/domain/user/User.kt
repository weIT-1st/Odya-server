package kr.weit.odya.domain.user

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.profilecolor.ProfileColor
import kr.weit.odya.support.domain.BaseTimeEntity
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "users")
@Where(clause = "withdraw_date is null")
@SQLDelete(sql = "update users set withdraw_date = sysdate where id = ?")
@SequenceGenerator(name = "USERS_SEQ_GENERATOR", sequenceName = "USERS_SEQ", initialValue = 1, allocationSize = 1)
class User(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USERS_SEQ_GENERATOR")
    val id: Long = 0L,

    @Column(nullable = false, updatable = false, unique = true)
    val username: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val socialType: SocialType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val userRole: UserRole,

    @Column
    val withdrawDate: LocalDateTime? = null,

    @JoinColumn(name = "profile_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE])
    val profile: Profile,

    userInformation: UserInformation,
) : BaseTimeEntity() {
    @Embedded
    var information: UserInformation = userInformation
        protected set

    constructor(
        id: Long = 0L,
        username: String,
        email: String?,
        nickname: String,
        phoneNumber: String? = null,
        gender: Gender,
        birthday: LocalDate,
        socialType: SocialType,
        userRole: UserRole = UserRole.ROLE_USER,
        profile: Profile,
    ) : this(
        id = id,
        username = username,
        socialType = socialType,
        withdrawDate = null,
        userRole = userRole,
        userInformation = UserInformation(email, nickname, phoneNumber, gender, birthday),
        profile = profile,
    )

    val email: String?
        get() = information.email

    val nickname: String
        get() = information.nickname

    val phoneNumber: String?
        get() = information.phoneNumber

    val gender: Gender
        get() = information.gender

    val birthday: LocalDate
        get() = information.birthday

    fun changePhoneNumber(phoneNumber: String) {
        information = information.copy(phoneNumber = phoneNumber)
    }

    fun changeEmail(email: String) {
        information = information.copy(email = email)
    }

    fun changeInformation(nickname: String) {
        information = information.copy(nickname = nickname)
    }

    fun changeProfile(profileName: String, originFileName: String, profileColor: ProfileColor) {
        profile.changeProfile(profileName, originFileName, profileColor)
    }

    fun getAgeRange(): Int {
        val now = LocalDate.now()
        var age = now.year - birthday.year
        if (birthday.plusYears(age.toLong()) < now) {
            age -= 1
        }
        return age / 10
    }
}
