package kr.weit.odya.domain.placeReview

import java.io.Serializable

data class PlaceReviewId(val placeId: String = "", val user: Long = -1) : Serializable
