package kr.weit.odya.support.test

import kr.weit.odya.config.JpaAuditingConfiguration
import kr.weit.odya.support.config.TestSecurityConfig
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.context.annotation.Import
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

class BaseTests {
    @ActiveProfiles("test")
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
    annotation class TestEnvironment

    @TestEnvironment
    @Target(AnnotationTarget.CLASS)
    @Import(JpaAuditingConfiguration::class)
    @Retention(AnnotationRetention.RUNTIME)
    @DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=none"])
    annotation class RepositoryTest

    @TestEnvironment
    @AutoConfigureRestDocs
    @Import(TestSecurityConfig::class)
    @ExtendWith(RestDocumentationExtension::class)
    annotation class UnitControllerTestEnvironment
}
