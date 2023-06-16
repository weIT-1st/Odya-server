package kr.weit.odya.support.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.util.regex.Pattern
import kotlin.reflect.KClass

private const val NICKNAME_REGEXP = "[ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9\\s]+"
private val NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEXP)

@MustBeDocumented
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [NicknameValidator::class])
annotation class NicknameValidation(
    val message: String = "유효하지 않은 닉네임 패턴입니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class NicknameValidator : ConstraintValidator<NicknameValidation, String?> {
    override fun initialize(contactNumber: NicknameValidation) {}
    override fun isValid(
        contactField: String?,
        cxt: ConstraintValidatorContext?
    ): Boolean {
        return NICKNAME_PATTERN.matcher(contactField).matches()
    }
}
