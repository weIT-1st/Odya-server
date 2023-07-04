package kr.weit.odya.support

import kr.weit.odya.domain.profilecolor.ProfileColor

const val TEST_PROFILE_COLOR_ID = 1L
const val TEST_OTHER_PROFILE_COLOR_ID = 2L
const val TEST_DEFAULT_COLOR_HEX = "#ffd42c"
const val TEST_NONE_COLOR_HEX = "NONE"
const val TEST_DEFAULT_RED = 255
const val TEST_DEFAULT_GREEN = 212
const val TEST_DEFAULT_BLUE = 44
const val TEST_NONE_RGB = 0

fun createProfileColor(profileColorId: Long = TEST_OTHER_PROFILE_COLOR_ID): ProfileColor = ProfileColor(
    id = profileColorId,
    colorHex = TEST_DEFAULT_COLOR_HEX,
    red = TEST_DEFAULT_RED,
    green = TEST_DEFAULT_GREEN,
    blue = TEST_DEFAULT_BLUE
)

fun createNoneProfileColor(profileColorId: Long = TEST_PROFILE_COLOR_ID): ProfileColor = ProfileColor(
    id = profileColorId,
    colorHex = TEST_NONE_COLOR_HEX,
    red = TEST_NONE_RGB,
    green = TEST_NONE_RGB,
    blue = TEST_NONE_RGB
)

fun createProfileColorList() = listOf(createNoneProfileColor(), createProfileColor())

fun createEmptyProfileColorList() = listOf<ProfileColor>()
