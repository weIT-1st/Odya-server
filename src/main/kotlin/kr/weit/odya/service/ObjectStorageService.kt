package kr.weit.odya.service

import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.model.BmcException
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails
import com.oracle.bmc.objectstorage.model.StorageTier
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import kr.weit.odya.config.properties.ObjectStorageProperties
import kr.weit.odya.util.getOrThrow
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.Date

@Profile("!test")
@Service
class ObjectStorageService(private val properties: ObjectStorageProperties) {
    private val configFile: ConfigFileReader.ConfigFile = ConfigFileReader.parse(properties.configurationFilePath)
    private val provider: AuthenticationDetailsProvider = ConfigFileAuthenticationDetailsProvider(configFile)
    private val client: ObjectStorageClient = ObjectStorageClient.builder().build(provider)
    private val saveRequestBuilder = PutObjectRequest.builder()
        .namespaceName(properties.namespaceName)
        .bucketName(properties.bucketName)
        .storageTier(StorageTier.Standard)
    private val deleteRequestBuilder = DeleteObjectRequest.builder()
        .namespaceName(properties.namespaceName)
        .bucketName(properties.bucketName)

    fun save(fileStream: InputStream, fileName: String) {
        val request = saveRequestBuilder
            .objectName(fileName)
            .putObjectBody(fileStream)
            .build()
        runCatching {
            client.putObject(request)
        }.onFailure {
            throw ObjectStorageException("$fileName: 저장에 실패했습니다(${it.message})")
        }
    }

    fun delete(fileName: String) {
        val request = deleteRequestBuilder
            .objectName(fileName)
            .build()
        runCatching {
            client.deleteObject(request)
        }.onFailure {
            require(!(it is BmcException && it.statusCode == HttpStatus.NOT_FOUND.value())) {
                "$fileName: Object Storage에 존재하지 않는 파일입니다"
            }
            throw ObjectStorageException("$fileName: 삭제에 실패했습니다(${it.message})")
        }
    }

    fun getPreAuthenticatedObjectUrl(objectName: String): String {
        val createPreAuthenticatedRequestRequest = getCreatePreAuthenticatedRequestRequest(objectName)
        val preAuthenticatedRequest = runCatching {
            client.createPreauthenticatedRequest(createPreAuthenticatedRequestRequest).preauthenticatedRequest
        }.getOrThrow {
            throw ObjectStorageException("$objectName: pre-authenticated request 생성에 실패했습니다")
        }
        return "${properties.objectGetUrl}${preAuthenticatedRequest.accessUri}"
    }

    private fun getCreatePreAuthenticatedRequestRequest(objectName: String): CreatePreauthenticatedRequestRequest? {
        val createRequestDetails = CreatePreauthenticatedRequestDetails.builder()
            .name(objectName)
            .objectName(objectName)
            .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead)
            .timeExpires(createPreAuthenticationRequestExpiredTime())
            .build()
        return CreatePreauthenticatedRequestRequest.builder()
            .namespaceName(properties.namespaceName)
            .bucketName(properties.bucketName)
            .createPreauthenticatedRequestDetails(createRequestDetails)
            .build()
    }

    private fun createPreAuthenticationRequestExpiredTime(): Date? =
        Date.from(Date().toInstant().plusSeconds(properties.preAuthenticatedRequestExpiredSec))
}
