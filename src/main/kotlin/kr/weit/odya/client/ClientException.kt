package kr.weit.odya.client

import kr.weit.odya.support.exception.ErrorCode

open class ClientException(val errorCode: ErrorCode, errorMessage: String?) : RuntimeException(errorMessage)

class KakaoClientException(errorMessage: String?) : ClientException(ErrorCode.KAKAO_CLIENT_EXCEPTION, errorMessage)
