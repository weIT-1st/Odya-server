package kr.weit.odya.support

import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ImageResponse
import kr.weit.odya.service.dto.SliceResponse

const val TEST_IMAGE_ID = 1L
const val TEST_INVALID_IMAGE_ID = -1L
const val TEST_IMAGE_URL: String = "testImageUrl"

val TEST_CONTENT_IMAGES = listOf(
    createContentImage(),
    createOtherContentImage(),
)

fun createContentImage(
    name: String = TEST_GENERATED_FILE_NAME,
    originName: String = TEST_IMAGE_FILE_WEBP,
    user: User = createUser(),
    isLifeShot: Boolean = false,
) = ContentImage(
    name = name,
    originName = originName,
    user = user,
).apply { if (isLifeShot) setLifeShot() }

fun createOtherContentImage(
    name: String = TEST_OTHER_GENERATED_FILE_NAME,
    originName: String = TEST_OTHER_IMAGE_FILE_WEBP,
    user: User = createUser(),
) = ContentImage(
    name = name,
    originName = originName,
    user = user,
)

fun createImageResponse() = ImageResponse.of(createContentImage(), TEST_IMAGE_URL)

fun createSliceImageResponse() = SliceResponse(
    hasNext = true,
    content = listOf(createImageResponse()),
)
