package kr.weit.odya.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("object")
data class ObjectStorageProperties(
    val configurationFilePath: String,
    val namespaceName: String,
    val bucketName: String,
    val preAuthenticatedRequestExpiredSec: Long,
    val objectGetUrl: String
)
