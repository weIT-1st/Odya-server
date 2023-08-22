package kr.weit.odya.support

import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.user.User

val TEST_CONTENT_IMAGES = listOf(
    createContentImage(),
    createOtherContentImage(),
)

fun createContentImage(
    name: String = TEST_GENERATED_FILE_NAME,
    originName: String = TEST_IMAGE_FILE_WEBP,
    user: User = createUser(),
) = ContentImage(
    name = name,
    originName = originName,
    user = user,
)

fun createOtherContentImage(
    name: String = TEST_OTHER_GENERATED_FILE_NAME,
    originName: String = TEST_OTHER_IMAGE_FILE_WEBP,
    user: User = createUser(),
) = ContentImage(
    name = name,
    originName = originName,
    user = user,
)
