package kr.weit.odya.domain.traveljournal

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Lob

private class CoordinatesDeserializer : JsonDeserializer<Coordinates>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Coordinates {
        val latitudes = mutableListOf<Double>()
        val longitudes = mutableListOf<Double>()
        p.text.split("|").forEach { it ->
            val (latitude, longitude) = it.split(",").map { it.toDouble() }
            latitudes.add(latitude)
            longitudes.add(longitude)
        }
        return Coordinates(latitudes, longitudes)
    }
}

private class CoordinatesSerializer : JsonSerializer<Coordinates>() {
    override fun serialize(coordinates: Coordinates, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(coordinates.value)
    }
}

@Embeddable
@JsonSerialize(using = CoordinatesSerializer::class)
@JsonDeserialize(using = CoordinatesDeserializer::class)
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

    fun splitCoordinates(): Pair<List<Double>, List<Double>> {
        val latitudes = mutableListOf<Double>()
        val longitudes = mutableListOf<Double>()
        this.value.split("|").forEach { it ->
            val (latitude, longitude) = it.split(",").map { it.toDouble() }
            latitudes.add(latitude)
            longitudes.add(longitude)
        }
        return Pair(latitudes, longitudes)
    }

    companion object {
        fun of(latitudes: List<Double>?, longitudes: List<Double>?): Coordinates? =
            if (latitudes != null && longitudes != null && latitudes.isNotEmpty()) { // latitudes와 longitudes가 같지 않으면 어차피 에러가 발생하므로 longitudes.isNotEmpty() 체크는 하지 않음
                Coordinates(
                    latitudes,
                    longitudes,
                )
            } else {
                null
            }
    }
}
