package kr.weit.odya.support.config

import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistoryRepository
import kr.weit.odya.service.ObjectStorageService
import org.opensearch.testcontainers.OpensearchContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.time.temporal.ChronoUnit

@TestConfiguration
class TestMockBeanConfig {
    @MockBean
    lateinit var objectStorageService: ObjectStorageService

    @MockBean
    lateinit var placeSearchHistoryRepository: PlaceSearchHistoryRepository
    companion object {

        @Container
        var opensearch: OpensearchContainer =
            OpensearchContainer(DockerImageName.parse("opensearchproject/opensearch:2.4.0"))
                .withStartupTimeout(Duration.of(200, ChronoUnit.SECONDS))
                .waitingFor(Wait.forListeningPort())
                .apply { start() }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of("opensearch.uris=" + opensearch.httpHostAddress)
                .applyTo(configurableApplicationContext.environment)
        }
    }
}
