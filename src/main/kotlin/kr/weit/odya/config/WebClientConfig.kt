package kr.weit.odya.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

private const val DEFAULT_TIME_OUT_MS: Long = 5000

@Configuration
class WebClientConfig {
    @Bean
    fun webClient() = WebClient
        .builder()
        .clientConnector(ReactorClientHttpConnector(httpClient()))
        .build()

    private fun httpClient() = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEFAULT_TIME_OUT_MS.toInt())
        .responseTimeout(Duration.ofMillis(DEFAULT_TIME_OUT_MS))
        .doOnConnected { connection ->
            connection
                .addHandlerFirst(ReadTimeoutHandler(DEFAULT_TIME_OUT_MS, TimeUnit.MILLISECONDS))
                .addHandlerLast(WriteTimeoutHandler(DEFAULT_TIME_OUT_MS, TimeUnit.MILLISECONDS))
        }
}
