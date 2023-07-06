package kr.weit.odya.service.generator

import org.springframework.stereotype.Component
import java.util.*

private const val DEFAULT_PROFILE_NAME_END_INDEX = 16

@Component
class FileNameGenerator {
    fun generate(): String = UUID.randomUUID().toString().take(DEFAULT_PROFILE_NAME_END_INDEX)
}
