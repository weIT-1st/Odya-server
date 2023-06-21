package kr.weit.odya.support.client

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.util.function.Consumer

@Component
class WebClientHelper(
    private val webClient: WebClient
) {
    fun <R> getWithHeader(uri: String, responseType: Class<R>, headersConsumer: Consumer<HttpHeaders>): R =
        webClient
            .get()
            .uri(uri)
            .headers(headersConsumer)
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError || httpStatusCode.is5xxServerError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .map { errorMessage -> WebClientException(errorMessage) }
            }
            .bodyToMono(responseType)
            .block() ?: throw WebClientResponseNullException()
}
