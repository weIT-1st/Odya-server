package kr.weit.odya.support.test

import kr.weit.odya.config.JpaAuditingConfiguration
import kr.weit.odya.config.QueryFactoryConfig
import kr.weit.odya.support.config.TestContainersConfig
import kr.weit.odya.support.config.TestMockBeanConfig
import kr.weit.odya.support.config.TestSecurityConfig
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestConstructor
import org.testcontainers.junit.jupiter.Testcontainers

class BaseTests {
    @ActiveProfiles("test")
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
    annotation class TestEnvironment

    @DataJpaTest
    @Testcontainers
    @TestEnvironment
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Import(JpaAuditingConfiguration::class, QueryFactoryConfig::class)
    @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    @ContextConfiguration(initializers = [TestContainersConfig.Initializer::class])
    annotation class RepositoryTest

    @TestEnvironment
    @AutoConfigureRestDocs
    @Import(TestSecurityConfig::class)
    @ExtendWith(RestDocumentationExtension::class)
    annotation class UnitControllerTestEnvironment

    @Testcontainers
    @SpringBootTest
    @TestEnvironment
    @Import(TestMockBeanConfig::class)
    @ContextConfiguration(initializers = [TestContainersConfig.Initializer::class])
    annotation class IntegrationTest
}
