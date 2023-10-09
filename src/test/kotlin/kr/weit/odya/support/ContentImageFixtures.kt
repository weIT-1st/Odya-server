package kr.weit.odya.support

import com.google.maps.model.Geometry
import com.google.maps.model.LatLng
import com.google.maps.model.PlaceDetails
import kr.weit.odya.domain.contentimage.ContentImage
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.CoordinateImageRequest
import kr.weit.odya.service.dto.CoordinateImageResponse
import kr.weit.odya.service.dto.ImageResponse
import kr.weit.odya.service.dto.ImageUserType
import kr.weit.odya.service.dto.SliceResponse

const val TEST_IMAGE_ID = 1L
const val TEST_OTHER_IMAGE_ID = 1L
const val TEST_INVALID_IMAGE_ID = -1L
const val TEST_IMAGE_URL: String = "testImageUrl"
const val TEST_PLACE_NAME: String = "testPlaceName"
const val TEST_LEFT_LONGITUDE: Double = 0.0
const val TEST_BOTTOM_LATITUDE: Double = 0.0
const val TEST_RIGHT_LONGITUDE: Double = 10.0
const val TEST_TOP_LATITUDE: Double = 10.0

const val LEFT_LONGITUDE_PARAM = "leftLongitude"
const val BOTTOM_LATITUDE_PARAM = "bottomLatitude"
const val RIGHT_LONGITUDE_PARAM = "rightLongitude"
const val TOP_LATITUDE_PARAM = "topLatitude"

val TEST_CONTENT_IMAGES = listOf(
    createContentImage(),
    createOtherContentImage(),
)

fun createContentImage(
    name: String = TEST_GENERATED_FILE_NAME,
    originName: String = TEST_IMAGE_FILE_WEBP,
    user: User = createUser(),
    isLifeShot: Boolean = false,
    placeDetails: PlaceDetails? = null,
    placeName: String? = TEST_PLACE_NAME,
) = ContentImage(
    name = name,
    originName = originName,
    user = user,
).apply {
    if (isLifeShot) setLifeShotInfo(placeName)
    if (placeDetails != null) setPlace(placeDetails)
}

fun createOtherContentImage(
    name: String = TEST_OTHER_GENERATED_FILE_NAME,
    originName: String = TEST_OTHER_IMAGE_FILE_WEBP,
    user: User = createUser(),
) = ContentImage(
    id = TEST_OTHER_IMAGE_ID,
    name = name,
    originName = originName,
    user = user,
)

fun createImageResponse() = ImageResponse.of(createContentImage(), TEST_IMAGE_URL)

fun createSliceImageResponse() = SliceResponse(
    hasNext = true,
    content = listOf(createImageResponse()),
)

fun createPlaceDetails(
    lat: Double = TEST_BOTTOM_LATITUDE,
    lng: Double = TEST_LEFT_LONGITUDE,
    placeId: String = TEST_PLACE_ID,
) = PlaceDetails().apply {
    this.placeId = placeId
    geometry = Geometry().apply { location = LatLng(lat, lng) }
}

fun createPlaceDetailsMap() = mapOf(TEST_PLACE_ID to createPlaceDetails())

fun createCoordinateImageRequest(
    leftLongitude: Double = TEST_LEFT_LONGITUDE,
    bottomLatitude: Double = TEST_BOTTOM_LATITUDE,
    rightLongitude: Double = TEST_RIGHT_LONGITUDE,
    topLatitude: Double = TEST_TOP_LATITUDE,
    size: Int = TEST_DEFAULT_SIZE,
) = CoordinateImageRequest(leftLongitude, bottomLatitude, rightLongitude, topLatitude, size)

fun createCoordinateImageResponse(
    imageId: Long = TEST_IMAGE_ID,
    userId: Long = TEST_USER_ID,
    imageUrl: String = TEST_IMAGE_URL,
    placeId: String = TEST_PLACE_ID,
    latitude: Double = TEST_BOTTOM_LATITUDE,
    longitude: Double = TEST_LEFT_LONGITUDE,
    imageUserType: ImageUserType = ImageUserType.USER,
    travelJournalId: Long? = TEST_TRAVEL_JOURNAL_ID,
    communityId: Long? = TEST_COMMUNITY_ID,
) = CoordinateImageResponse(
    imageId,
    userId,
    imageUrl,
    placeId,
    latitude,
    longitude,
    imageUserType,
    travelJournalId,
    communityId,
)

fun createCoordinateImageResponseList() = listOf(createCoordinateImageResponse())
