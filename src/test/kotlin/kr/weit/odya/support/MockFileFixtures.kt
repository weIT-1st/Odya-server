package kr.weit.odya.support

import org.springframework.mock.web.MockMultipartFile

const val TEST_FILE_AUTHENTICATED_URL: String = "testFileAuthenticatedUrl"
const val TEST_PROFILE_URL: String = "testProfileUrl"
const val TEST_IMAGE_FILE_CONTENT_TYPE = "image/png"
const val TEST_FILE_NAME = "example_file"
const val TEST_GENERATED_FILE_NAME = "generated_file.png"
const val TEST_OTHER_GENERATED_FILE_NAME = "generated_other_file.png"
const val TEST_IMAGE_FILE_PNG = "example_file.png"
const val TEST_OTHER_IMAGE_FILE_PNG = "example_file_other.png"
const val TEST_DEFAULT_PROFILE_NAME = "default_profile"
const val TEST_DEFAULT_PROFILE_PNG = "default_profile.png"
const val TEST_INVALID_IMAGE_FILE_ORIGINAL_NAME = "example_file.invalid"
const val TEST_INVALID_PROFILE_ORIGINAL_NAME = "default_profile.invalid"
const val TEST_PROFILE_PNG = "example.png"
val TEST_FILE_IMAGE_CONTENT_BYTE_ARRAY = "example".byteInputStream()
val TEST_PROFILE_CONTENT_BYTE_ARRAY = "example".byteInputStream()
const val TEST_MOCK_PROFILE_NAME = "profile"
const val TEST_MOCK_FILE_NAME = "travel-journal-content-image"
const val TEST_TRAVEL_JOURNAL_REQUEST_NAME = "travel-journal"
const val TEST_FILE_CONTENT_TYPE = "application/json"

fun createMockProfile(
    name: String = TEST_MOCK_PROFILE_NAME,
    originalFileName: String? = TEST_DEFAULT_PROFILE_PNG,
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
    name: String = TEST_MOCK_FILE_NAME,
    originalFileName: String? = TEST_IMAGE_FILE_PNG,
    contentType: String? = TEST_IMAGE_FILE_CONTENT_TYPE,
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        TEST_FILE_IMAGE_CONTENT_BYTE_ARRAY,
    )
}

fun createMockOtherImageFile(
    name: String = TEST_MOCK_FILE_NAME,
    originalFileName: String? = TEST_OTHER_IMAGE_FILE_PNG,
    contentType: String? = TEST_IMAGE_FILE_CONTENT_TYPE,
): MockMultipartFile {
    return MockMultipartFile(
        name,
        originalFileName,
        contentType,
        TEST_FILE_IMAGE_CONTENT_BYTE_ARRAY,
    )
}

fun createMockImageFiles() = listOf(
    createMockImageFile(),
    createMockOtherImageFile(),
)
