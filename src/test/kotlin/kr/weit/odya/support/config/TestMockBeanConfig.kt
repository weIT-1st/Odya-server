package kr.weit.odya.support.config

import kr.weit.odya.client.GoogleMapsClient
import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistoryRepository
import kr.weit.odya.domain.user.UsersDocumentRepository
import kr.weit.odya.service.ObjectStorageService
import org.redisson.api.RedissonClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean

@TestConfiguration
class TestMockBeanConfig {
    @MockBean
    lateinit var objectStorageService: ObjectStorageService

    @MockBean
    lateinit var placeSearchHistoryRepository: PlaceSearchHistoryRepository

    @MockBean
    lateinit var usersDocumentRepository: UsersDocumentRepository

    @MockBean
    lateinit var googleMapsClient: GoogleMapsClient

    @MockBean
    lateinit var redissonClient: RedissonClient
}
