package kr.weit.odya.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("open-search")
data class OpenSearchProperties(
    val serverUrl: String,
    val username: String,
    val password: String,
    val port: Int = 443,
    val scheme: String = "https",
)
