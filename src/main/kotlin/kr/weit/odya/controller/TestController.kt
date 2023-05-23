package kr.weit.odya.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @PostMapping("/test")
    fun test(@RequestBody name: String): Pair<Long, String> {
        val trimedName = name.trim()
        if (trimedName.isEmpty()) {
            throw IllegalArgumentException("이름이 비어있으면 안됨")
        }
        if (trimedName == "김한빈") {
            throw IllegalArgumentException("김한빈은 안됨")
        }
        return Pair(trimedName.hashCode().toLong(), trimedName)
    }
}