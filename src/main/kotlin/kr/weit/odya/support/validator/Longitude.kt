package kr.weit.odya.support.validator

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import kotlin.reflect.KClass

@Min(-180)
@Max(180)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [])
annotation class Longitude(
    val message: String = "경도는 -180 ~ 180 사이의 값이어야 합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)
