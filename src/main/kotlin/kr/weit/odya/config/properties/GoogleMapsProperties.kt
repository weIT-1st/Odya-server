package kr.weit.odya.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile

@Profile("!test")
@ConfigurationProperties("google.maps")
class GoogleMapsProperties(
    val apiKey: String,
)
