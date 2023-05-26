package support.test

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor

class BaseTests {
    @ActiveProfiles("test")
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
    annotation class TestEnvironment
}
