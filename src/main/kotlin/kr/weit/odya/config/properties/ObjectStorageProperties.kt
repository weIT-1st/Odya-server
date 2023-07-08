package kr.weit.odya.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile

@Profile("!test")
@ConfigurationProperties("object")
data class ObjectStorageProperties(
    val configurationFilePath: String,
    val namespaceName: String,
    val bucketName: String,
)
