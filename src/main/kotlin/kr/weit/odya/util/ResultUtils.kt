package kr.weit.odya.util

fun <T> Result<T>.getOrThrow(action: (exception: Throwable) -> Unit): T {
    onFailure { action(it) }
    return getOrThrow()
}
