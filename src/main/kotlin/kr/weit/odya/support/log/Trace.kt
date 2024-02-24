package kr.weit.odya.support.log

data class Trace(
    val timestamp: String,
    val traceRequest: TraceRequest,
    val traceResponse: TraceResponse,
    val elapsed: Long,
    val path: String,
    val method: String,
    val remoteAddress: String,
    val cause: String? = null,
    val errorType: Class<Throwable>? = null,
    val checkpoints: List<String>? = null,
    val logType: LogType,
)

enum class LogType {
    protocol,
    error,
}

data class TraceRequest(
    val headers: Map<String, List<String>>,
    val params: Map<String, Any>,
    val body: Map<String, Any>,
)

data class TraceResponse(
    val status: Int,
    val headers: Map<String, List<String>>,
    val body: Map<String, Any>,
)
