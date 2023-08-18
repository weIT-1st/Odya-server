package kr.weit.odya.domain.traveljournal

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Lob

@Embeddable
class Coordinates(
    latitudes: List<Double>,
    longitudes: List<Double>,
) {
    @Lob
    @Column(nullable = false, columnDefinition = "CLOB")
    val value: String = latitudes.zip(longitudes).joinToString("|") { (lat, lon) -> "$lat,$lon" }

    init {
        require(latitudes.size == longitudes.size) { "위도(${latitudes.size})와 경도(${longitudes.size})의 개수가 일치하지 않습니다." }
    }
}
