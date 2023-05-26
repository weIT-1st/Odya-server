package kr.weit.odya.controller

import kr.weit.odya.service.dto.TestRequest
import kr.weit.odya.service.dto.TestResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    // TODO: 이 API는 클라이언트와의 연동 + RestDocs 테스트를 위한 임시 API이므로, 추후 삭제해야 함
    @PostMapping("/test")
    fun test(@RequestBody testRequest: TestRequest): TestResponse {
        val trimedName = testRequest.name.trim()
        if (trimedName.isEmpty()) {
            throw IllegalArgumentException("이름이 비어있으면 안됨")
        }
        if (trimedName == "김한빈") {
            throw IllegalArgumentException("김한빈은 안됨")
        }
        return TestResponse(trimedName.hashCode(), trimedName)
    }
}
