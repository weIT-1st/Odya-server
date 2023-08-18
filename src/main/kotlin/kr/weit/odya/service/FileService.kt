package kr.weit.odya.service

import kr.weit.odya.service.generator.FileNameGenerator
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.Locale

@Service
class FileService(
    private val fileNameGenerator: FileNameGenerator,
    private val objectStorageService: ObjectStorageService,
) {
    fun saveFile(file: MultipartFile): String = getFileName(file.originalFilename).also {
        objectStorageService.save(file.inputStream, it)
    }

    fun getPreAuthenticatedObjectUrl(profileName: String): String =
        objectStorageService.getPreAuthenticatedObjectUrl(profileName)

    fun deleteFile(profileName: String) = objectStorageService.delete(profileName)

    private fun getFileFormat(originFileName: String): String {
        return originFileName.let {
            it.substring(it.lastIndexOf(".") + 1).lowercase(Locale.getDefault()).apply {
                require(ALLOW_FILE_FORMAT_LIST.contains(this)) {
                    "프로필 사진은 ${ALLOW_FILE_FORMAT_LIST.joinToString()} 형식만 가능합니다"
                }
            }
        }
    }

    private fun getFileName(originFileName: String?): String {
        require(originFileName != null) { "원본 파일 이름이 존재하지 않습니다" }
        return "${fileNameGenerator.generate()}.${getFileFormat(originFileName)}"
    }
}
