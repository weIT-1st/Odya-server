package kr.weit.odya.support.validator

import jakarta.validation.Constraint
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlin.reflect.KClass

@Min(-90)
@Max(90)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class Latitude(
    val message: String = "위도는 -90 ~ 90 사이의 값이어야 합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<Any>> = [],
)
