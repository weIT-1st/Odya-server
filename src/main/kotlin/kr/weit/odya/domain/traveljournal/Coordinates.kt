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

private class PasswordDeserializer : JsonDeserializer<Coordinates>() {
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

private class PasswordSerializer : JsonSerializer<Coordinates>() {
    override fun serialize(coordinates: Coordinates, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(coordinates.value)
    }
}

@Embeddable
@JsonSerialize(using = PasswordSerializer::class)
@JsonDeserialize(using = PasswordDeserializer::class)
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
