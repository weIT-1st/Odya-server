package kr.weit.odya.support.log

import mu.KLogger
import mu.KotlinLogging

class Logger {
    companion object {
        private val logger: KLogger = KotlinLogging.logger {}
        fun error(t: Throwable?, msg: () -> Any?) {
            logger.error(t) { msg() }
        }
    }
}
