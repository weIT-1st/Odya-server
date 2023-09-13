package kr.weit.odya.support

import com.google.maps.model.Geometry
import com.google.maps.model.LatLng
import com.google.maps.model.PlaceDetails
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ImageResponse
import kr.weit.odya.service.dto.SliceResponse

const val TEST_IMAGE_ID = 1L
const val TEST_INVALID_IMAGE_ID = -1L
const val TEST_IMAGE_URL: String = "testImageUrl"
const val TEST_PLACE_NAME: String = "testPlaceName"

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
).apply { if (isLifeShot) setLifeShotInfo(TEST_PLACE_NAME) }

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

fun createPlaceDetails() = PlaceDetails().apply {
    geometry = Geometry().apply { location = LatLng(0.0, 0.0) }
}

fun createPlaceDetailsMap() = mapOf(TEST_PLACE_ID to createPlaceDetails())
