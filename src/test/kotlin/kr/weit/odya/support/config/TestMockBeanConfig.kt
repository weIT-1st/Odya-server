package kr.weit.odya.support.config

import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistoryRepository
import kr.weit.odya.service.ObjectStorageService
import org.opensearch.testcontainers.OpensearchContainer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.OracleContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.utility.DockerImageName
import java.time.Duration

@TestConfiguration
class TestMockBeanConfig {
    @MockBean
    lateinit var objectStorageService: ObjectStorageService

    @MockBean
    lateinit var placeSearchHistoryRepository: PlaceSearchHistoryRepository
    companion object {

        @Container
        var opensearch: OpensearchContainer =
            OpensearchContainer(DockerImageName.parse("opensearchproject/opensearch:2.8.0"))
                .withStartupAttempts(5)
                .withStartupTimeout(Duration.ofMinutes(2))
                .apply { start() }

        @Container
        var oracleDB: OracleContainer = OracleContainer("gvenzl/oracle-xe")
            .withDatabaseName("tests-db")
            .withUsername("sa")
            .withPassword("sa")
            .waitingFor(Wait.forListeningPort())
            .apply { start() }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of("opensearch.uris=" + opensearch.httpHostAddress)
                .applyTo(configurableApplicationContext.environment)
            val values = TestPropertyValues.of(
                "spring.datasource.url=" + oracleDB.jdbcUrl,
                "spring.datasource.password=" + oracleDB.password,
                "spring.datasource.username=" + oracleDB.username,
            )
        }
    }
}
