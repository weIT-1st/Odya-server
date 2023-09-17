package kr.weit.odya.support.validator

import jakarta.validation.Constraint
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlin.reflect.KClass

@Min(-90, message = "위도는 -90 이상이어야 합니다.")
@Max(90, message = "위도는 90 이하여야 합니다.")
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class Latitude(
    val message: String = "",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Any>> = [],
)
