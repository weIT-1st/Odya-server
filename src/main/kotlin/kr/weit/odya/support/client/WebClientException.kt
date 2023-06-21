package kr.weit.odya.support.client

open class WebClientException(message: String? = null) : Exception(message)

class WebClientResponseNullException(message: String = "WebClient 응답 값이 존재하지 않습니다") : WebClientException(message)
