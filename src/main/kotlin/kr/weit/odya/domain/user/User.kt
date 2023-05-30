package kr.weit.odya.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
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
    val id: Long,

    @Column(nullable = false, updatable = false)
    val username: String,

    @Embedded
    val information: UserInformation,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val socialType: SocialType,

    @Column
    val withdrawDate: LocalDateTime? = null,

    @Column(nullable = false, updatable = false)
    val createdDate: LocalDateTime = LocalDateTime.now()
) {
    constructor(
        username: String,
        email: String,
        nickname: String,
        phoneNumber: String? = null,
        gender: Gender,
        birthday: LocalDate,
        profileName: String = "default_profile.png",
        socialType: SocialType
    ) : this(
        id = 0L,
        username = username,
        information = UserInformation(email, nickname, phoneNumber, gender, birthday, profileName),
        socialType = socialType,
        withdrawDate = null
    )

    val email: String
        get() = information.email

    val nickname: String
        get() = information.nickname

    val phoneNumber: String?
        get() = information.phoneNumber

    val gender: Gender
        get() = information.gender

    val birthday: LocalDate
        get() = information.birthday

    val profileName: String
        get() = information.profileName
}
