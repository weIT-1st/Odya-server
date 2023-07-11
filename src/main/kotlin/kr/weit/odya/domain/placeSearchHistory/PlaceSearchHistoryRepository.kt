package kr.weit.odya.domain.placeSearchHistory

import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository

@Profile("!test")
@Repository
interface PlaceSearchHistoryRepository : ElasticsearchRepository<PlaceSearchHistory, Long>
