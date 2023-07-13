package kr.weit.odya.domain.placeSearchHistory

import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder
import org.opensearch.data.client.orhlc.OpenSearchAggregations
import org.opensearch.index.query.BoolQueryBuilder
import org.opensearch.index.query.QueryBuilders.boolQuery
import org.opensearch.index.query.QueryBuilders.matchQuery
import org.opensearch.index.query.QueryBuilders.rangeQuery
import org.opensearch.search.aggregations.AggregationBuilders
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Repository
interface PlaceSearchHistoryRepository : ElasticsearchRepository<PlaceSearchHistory, Long>, CustomPlaceSearchHistoryRepository

interface CustomPlaceSearchHistoryRepository {
    fun getRecentTop10Keywords(ageRange: Int?): List<String>
}

class CustomPlaceSearchHistoryRepositoryImpl(private val elasticsearchOperations: ElasticsearchOperations) : CustomPlaceSearchHistoryRepository {
    override fun getRecentTop10Keywords(ageRange: Int?): List<String> {
        val agg = AggregationBuilders.terms("GroupBySearchTerm")
            .field("searchTerm").size(10)
        val query = NativeSearchQueryBuilder().withQuery(
            getSearchCondition(ageRange),
        ).withAggregations(agg).build()

        val searchResult = (elasticsearchOperations.search(query, PlaceSearchHistory::class.java).aggregations as OpenSearchAggregations)
            .aggregations().get<ParsedStringTerms>("GroupBySearchTerm")
        return searchResult.buckets.map {
            (it.key as String).let { key -> key.substring(0, key.lastIndexOf("/")) }
        }.toList()
    }

    private fun getSearchCondition(ageRange: Int?): BoolQueryBuilder {
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
        val query = boolQuery()
            .must(
                rangeQuery("@timestamp")
                    .gte(now.minusDays(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .lte(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()),
            )
        if (ageRange != null) {
            return query.must(matchQuery("ageRange", ageRange))
        }
        return query
    }
}
