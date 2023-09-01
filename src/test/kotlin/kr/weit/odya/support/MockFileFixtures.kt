package kr.weit.odya.support

import org.springframework.mock.web.MockMultipartFile

const val TEST_FILE_AUTHENTICATED_URL: String = "testFileAuthenticatedUrl"
const val TEST_PROFILE_URL: String = "testProfileUrl"
const val TEST_IMAGE_FILE_CONTENT_TYPE = "image/webp"
const val TEST_FILE_NAME = "example_file"
const val TEST_GENERATED_FILE_NAME = "generated_file.webp"
const val TEST_OTHER_GENERATED_FILE_NAME = "generated_other_file.webp"
const val TEST_IMAGE_FILE_WEBP = "example_file.webp"
const val TEST_OTHER_IMAGE_FILE_WEBP = "example_file_other.webp"
const val TEST_DEFAULT_PROFILE_NAME = "default_profile"
const val TEST_DEFAULT_PROFILE_PNG = "default_profile.png"
const val TEST_INVALID_IMAGE_FILE_ORIGINAL_NAME = "example_file.invalid"
const val TEST_INVALID_PROFILE_ORIGINAL_NAME = "default_profile.invalid"
const val TEST_PROFILE_WEBP = "example.webp"
val TEST_FILE_IMAGE_CONTENT_BYTE_ARRAY = "example".byteInputStream()
val TEST_PROFILE_CONTENT_BYTE_ARRAY = "example".byteInputStream()
const val TEST_MOCK_PROFILE_NAME = "profile"
const val TEST_FILE_CONTENT_TYPE = "application/json"

fun createMockProfile(
    name: String = TEST_MOCK_PROFILE_NAME,
    originalFileName: String? = TEST_PROFILE_WEBP,
    contentType: String? = TEST_IMAGE_FILE_CONTENT_TYPE,
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        TEST_PROFILE_CONTENT_BYTE_ARRAY,
    )
}

fun createMockImageFile(
    mockFileName: String,
    originalFileName: String? = TEST_IMAGE_FILE_WEBP,
    contentType: String? = TEST_IMAGE_FILE_CONTENT_TYPE,
): MockMultipartFile {
    return MockMultipartFile(
        mockFileName,
        originalFileName,
        contentType,
        TEST_FILE_IMAGE_CONTENT_BYTE_ARRAY,
    )
}

fun createMockOtherImageFile(
    mockFileName: String,
    originalFileName: String? = TEST_OTHER_IMAGE_FILE_WEBP,
    contentType: String? = TEST_IMAGE_FILE_CONTENT_TYPE,
): MockMultipartFile {
    return MockMultipartFile(
        mockFileName,
        originalFileName,
        contentType,
        TEST_FILE_IMAGE_CONTENT_BYTE_ARRAY,
    )
}

fun createMockImageFiles(mockFileName: String) = listOf(
    createMockImageFile(mockFileName),
    createMockOtherImageFile(mockFileName),
)
