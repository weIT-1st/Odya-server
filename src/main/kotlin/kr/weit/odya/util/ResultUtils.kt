package kr.weit.odya.util

fun <T> Result<T>.getOrThrow(action: () -> Unit): T {
    onFailure { action() }
    return getOrThrow()
}
