package kr.weit.odya.domain.user

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDate
import java.util.regex.Pattern

private const val EMAIL_REGEX: String = "\\w+@\\w+\\.\\w+(\\.\\w+)?"
private val emailPattern: Pattern = Pattern.compile(EMAIL_REGEX)

private const val PHONE_NUMBER_REGEX: String = "^\\d{3}-\\d{3,4}-\\d{4}$"
private val phoneNumberPattern = Pattern.compile(PHONE_NUMBER_REGEX)

@Embeddable
data class UserInformation(
    @Column
    val email: String?,

    @Column(nullable = false, length = 24, unique = true)
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
) {
    init {
        email?.run {
            identify(emailPattern.matcher(this).matches()) { "$this: 올바른 이메일 형식이 아닙니다" }
        }

        phoneNumber?.run {
            identify(phoneNumberPattern.matcher(this).matches()) { "$this: 올바른 휴대전화 형식이 아닙니다" }
        }
    }

    private fun identify(value: Boolean, message: () -> Any = {}) {
        if (!value) {
            throw IllegalArgumentException(message().toString())
        }
    }
}
