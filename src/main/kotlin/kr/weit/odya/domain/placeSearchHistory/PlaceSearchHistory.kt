package kr.weit.odya.domain.placeSearchHistory

import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.util.Date

@Document(indexName = "#{@environment.getProperty('open-search.indices.place-search-history')}")
data class PlaceSearchHistory(
    @Field(name = "searchTerm", type = FieldType.Text)
    val searchTerm: String,
    @Field(name = "ageRange", type = FieldType.Integer)
    val ageRange: Int,
    @Field(name = "@timestamp", type = FieldType.Date)
    val createdAt: Date = Date(),
    @Id
    @GeneratedValue
    val id: String? = null,
)
