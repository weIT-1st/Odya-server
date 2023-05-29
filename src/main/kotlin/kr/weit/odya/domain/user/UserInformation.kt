package kr.weit.odya.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDate

@Embeddable
data class UserInformation(
    @Column(nullable = false)
    val email: String,

    @Column(nullable = false, length = 20)
    val nickname: String,

    @Column(length = 13)
    val phoneNumber: String?,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val gender: Gender,

    @Column(nullable = false, updatable = false)
    val birthday: LocalDate,

    @Column(nullable = false)
    val profileName: String
)
