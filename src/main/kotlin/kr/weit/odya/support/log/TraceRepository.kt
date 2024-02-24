package kr.weit.odya.support.log

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.sentry.Sentry
import io.sentry.SentryLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension
import org.springframework.boot.actuate.web.exchanges.HttpExchange
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository
import org.springframework.boot.actuate.web.exchanges.HttpExchangesEndpoint
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import java.util.Collections
import java.util.LinkedList
val traceLogObjectMapper = jacksonObjectMapper().apply { this.propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE }

@Component
class TraceRepository(
    private val traceManager: TraceManager,
) : HttpExchangeRepository {
    private val contents: MutableList<Trace> = Collections.synchronizedList(LinkedList())

    private val errorLogger: Logger = LoggerFactory.getLogger("error")
    private val protocolLogger: Logger = LoggerFactory.getLogger("protocol")
    private val matcher = AntPathMatcher()

    override fun add(httpTrace: HttpExchange?) {
        if (httpTrace != null) {
            val trace = traceManager.apply { this.httpTrace = httpTrace }.getTrace()
            contents.add(trace)

            if (traceManager.isErrorLog()) {
                errorLogger.error(traceLogObjectMapper.writeValueAsString(trace))
                capture(traceManager.throwable!!, SentryLevel.ERROR, traceManager.getTrace().path)
            } else {
                if (!filterTrace(trace)) {
                    protocolLogger.info(traceLogObjectMapper.writeValueAsString(trace))
                }
            }
        }

        while (contents.size > 100) {
            contents.removeAt(0)
        }
    }

    private fun filterTrace(trace: Trace): Boolean {
        val filterList = listOf("/health", "/ready","/actuator/prometheus", "/**/swagger-ui/**", "/**/swagger-resources/**")

        return filterList.any { matcher.match(it, trace.path) }
    }

    override fun findAll(): List<HttpExchange> {
        return Collections.emptyList()
    }

    fun findAllTrace(): List<Trace> {
        return Collections.unmodifiableList(contents).reversed()
    }

    fun capture(throwable: Throwable, level: SentryLevel = SentryLevel.ERROR, path: String? = null) {
        if (path == "/ready") {
            return
        }
        Sentry.withScope {
            it.level = level
            Sentry.captureException(throwable)
        }
    }
}

@Component
@EndpointWebExtension(endpoint = HttpExchangesEndpoint::class)
class HttpTraceEndpointExtension(val repository: TraceRepository) {
    @ReadOperation
    fun readTraces(): ContentTraceDescriptor {
        return ContentTraceDescriptor(repository.findAllTrace())
    }
}

data class ContentTraceDescriptor(val traces: List<Trace>)
