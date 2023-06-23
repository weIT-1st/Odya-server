package kr.weit.odya.client.kakao

import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange("https://kapi.kakao.com")
interface KakaoClient {
    @GetExchange("/v2/user/me")
    fun getKakaoUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) accessToken: String): KakaoUserInfo
}
