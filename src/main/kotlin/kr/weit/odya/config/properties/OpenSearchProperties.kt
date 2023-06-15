package kr.weit.odya.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile

@Profile("!test")
@ConfigurationProperties("open-search")
data class OpenSearchProperties(
    val serverUrl: String,
    val username: String,
    val password: String
)
