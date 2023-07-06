package kr.weit.odya.support.config

import kr.weit.odya.service.ObjectStorageService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean

@TestConfiguration
class TestMockBeanConfig {
    @MockBean
    lateinit var objectStorageService: ObjectStorageService
}
