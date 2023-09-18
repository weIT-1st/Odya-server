package kr.weit.odya.client

import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.model.PlaceDetails
import kr.weit.odya.config.properties.GoogleMapsProperties
import org.springframework.stereotype.Component

@Component
class GoogleMapsClient(
    googleMapsProperties: GoogleMapsProperties,
) {
    private val apiContext = GeoApiContext.Builder()
        .apiKey(googleMapsProperties.apiKey)
        .build()

    fun findPlaceDetailsByPlaceId(placeId: String): PlaceDetails {
        return PlacesApi.placeDetails(apiContext, placeId).await()
    }
}
