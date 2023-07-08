package kr.weit.odya.support.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.regex.Pattern
import kotlin.reflect.KClass

private const val PHONE_NUMBER_REGEXP = "^\\d{3}-\\d{3,4}-\\d{4}$"
private val PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEXP)

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [PhoneNumberValidator::class])
annotation class PhoneNumber(
    val message: String = "유효하지 않은 전화번호 패턴입니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class PhoneNumberValidator : ConstraintValidator<PhoneNumber, String?> {
    override fun initialize(contactNumber: PhoneNumber) {}
    override fun isValid(
        contactField: String?,
        cxt: ConstraintValidatorContext?,
    ): Boolean {
        return contactField == null || PHONE_NUMBER_PATTERN.matcher(contactField).matches()
    }
}
