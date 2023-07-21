package kr.weit.odya.support.config

import org.opensearch.testcontainers.OpensearchContainer
import org.springframework.boot.sql.init.DatabaseInitializationMode
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

@TestConfiguration
class TestContainersConfig {
    companion object {
        private const val ORACLE_SID = "xe"
        private const val ORACLE_USER = "system"
        private const val ORACLE_PWD = "oracle"
        private const val OPENSEARCH_SERVER_URL = "localhost"
        private const val OPENSEARCH_SCHEME = "http"

        // 테스트 컨텍스트가 변하면 sql 초기화를 다시 시도하기 때문에 이를 방지하기 위한 플래그
        private val isSQLInit = AtomicBoolean(false)

        @Container
        val oracleContainer: GenericContainer<*> = GenericContainer("konempty/oracle-database:0.0.1")
            .withExposedPorts(1521)
            .waitingFor(Wait.forLogMessage(".*DATABASE IS READY TO USE!.*", 1))

        @Container
        val opensearchContainer: OpensearchContainer =
            OpensearchContainer("opensearchproject/opensearch:2.4.0")
                .waitingFor(Wait.forListeningPort())
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            // 컨테이너를 2개 다 띄우도록 비동기로 요청을 보내고 뜰때까지 기다린다.
            CompletableFuture.allOf(
                CompletableFuture.runAsync { oracleContainer.start() },
                CompletableFuture.runAsync { opensearchContainer.start() },
            ).get()

            val oraclePort: Int = oracleContainer.getMappedPort(1521)
            val properties = mapOf<String, String>(
                "spring.datasource.url" to "jdbc:oracle:thin:@//" + oracleContainer.host + ":" + oraclePort + "/" + ORACLE_SID,
                "spring.datasource.username" to ORACLE_USER,
                "spring.datasource.password" to ORACLE_PWD,
                "spring.sql.init.mode" to (if (isSQLInit.get()) DatabaseInitializationMode.NEVER else DatabaseInitializationMode.ALWAYS).toString(),

                "open-search.server-url" to OPENSEARCH_SERVER_URL,
                "open-search.user-name" to opensearchContainer.username,
                "open-search.password" to opensearchContainer.password,
                "open-search.port" to opensearchContainer.getMappedPort(9200).toString(),
                "open-search.scheme" to OPENSEARCH_SCHEME,
            )
            isSQLInit.set(true)

            TestPropertyValues.of(properties).applyTo(configurableApplicationContext.environment)
        }
    }
}
