package kr.weit.odya.client

import com.google.maps.GeoApiContext
import com.google.maps.PlacesApi
import com.google.maps.model.LatLng
import kr.weit.odya.config.properties.GoogleMapsProperties
import org.springframework.stereotype.Component

@Component
class GoogleMapsClient(
    googleMapsProperties: GoogleMapsProperties,
) {
    private val apiContext = GeoApiContext.Builder()
        .apiKey(googleMapsProperties.apiKey)
        .build()

    fun findCoordinateByPlaceId(placeId: String): LatLng {
        return PlacesApi.placeDetails(apiContext, placeId).await().geometry.location
    }
}
