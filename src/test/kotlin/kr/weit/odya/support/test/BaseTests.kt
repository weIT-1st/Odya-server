package kr.weit.odya.support.test

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
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
    @AutoConfigureRestDocs
    @ExtendWith(RestDocumentationExtension::class)
    annotation class UnitControllerTestEnvironment
}
