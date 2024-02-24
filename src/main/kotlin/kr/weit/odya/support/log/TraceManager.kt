package kr.weit.odya.support.log

import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.actuate.web.exchanges.HttpExchange
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.Charset

@Component
@RequestScope
class TraceManager {
    var throwable: Throwable? = null
    private val checkpoints = mutableListOf<String>()

    lateinit var httpTrace: HttpExchange
    lateinit var wrappedRequest: ContentCachingRequestWrapper
    lateinit var wrappedResponse: ContentCachingResponseWrapper

    fun doErrorLog(throwable: Throwable) {
        this.throwable = throwable
    }

    fun isErrorLog() = throwable != null

    fun getTrace(): Trace {
        val request = TraceRequest(
            headers = httpTrace.request.headers,
            params = queryToMap(httpTrace.request.uri.query),
            body = bodyToMap(wrappedRequest.contentAsByteArray.toString(Charset.defaultCharset())),
        )

        val response = TraceResponse(
            status = httpTrace.response.status,
            headers = httpTrace.response.headers,
            body = bodyToMap(wrappedResponse.contentAsByteArray.toString(Charset.defaultCharset())),
        )

        return Trace(
            timestamp = httpTrace.timestamp.toString(),
            traceRequest = request,
            traceResponse = response,
            elapsed = httpTrace.timeTaken.toMillis(),
            path = httpTrace.request.uri.path,
            method = httpTrace.request.method,
            remoteAddress = wrappedRequest.remoteAddr,
            cause = throwableToStackTrace(throwable),
            errorType = throwable?.javaClass,
            checkpoints = if (checkpoints.isEmpty()) null else checkpoints,
            logType = if (isErrorLog()) LogType.error else LogType.protocol,
        )
    }

    private fun queryToMap(query: String?): Map<String, Any> {
        if (query.isNullOrBlank()) {
            return emptyMap()
        }

        return query.split("&").associate {
            val param = it.split("=")
            param.first() to param.last()
        }
    }

    private fun bodyToMap(body: String?): MutableMap<String, Any> {
        if (body.isNullOrBlank()) {
            return mutableMapOf()
        }

        return try {
            traceLogObjectMapper.readValue(body)
        } catch (e: Exception) {
            return mutableMapOf("original" to body)
        }
    }

    companion object {
        fun throwableToStackTrace(throwable: Throwable?): String? {
            if (throwable == null) {
                return null
            }

            val stackTrace = StringWriter()
            throwable.printStackTrace(PrintWriter(stackTrace))
            stackTrace.flush()

            return stackTrace.toString()
        }
    }
}
