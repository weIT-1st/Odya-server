package kr.weit.odya.service

import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.StorageTier
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.responses.DeleteObjectResponse
import com.oracle.bmc.objectstorage.responses.PutObjectResponse
import kr.weit.odya.config.properties.ObjectStorageProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.InputStream

@Profile("!test")
@Service
class ObjectStorageService(properties: ObjectStorageProperties) {
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

    /*
    * filestream : 이름은 명시적으로 파일이라고 했지만 사실 꼭 파일 Stream은 아니어도 됩니다
    * fileName : 파일 이름 (확장자 포함) 파일이름이 중복되면 기존 파일이 덮어씌워지므로 매 파일마다 Unique한 이름을 지정해야합니다
    */
    fun save(fileStream: InputStream, fileName: String): PutObjectResponse? {
        val request = saveRequestBuilder
            .objectName(fileName)
            .putObjectBody(fileStream).build()

        return client.putObject(request)
    }

    fun delete(fileName: String): DeleteObjectResponse? {
        val request = deleteRequestBuilder
            .objectName(fileName)
            .build()
        return client.deleteObject(request)
    }
}
