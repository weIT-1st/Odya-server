package kr.weit.odya.service.generator

import org.springframework.stereotype.Component
import kotlin.random.Random

private const val DEFAULT_RANDOM_PROFILE_START_INDEX = 1

@Component
class ProfileColorRandomIndexGenerator {
    fun getRandomNumber(size: Int): Int {
        return Random.nextInt(DEFAULT_RANDOM_PROFILE_START_INDEX, size)
    }
}
