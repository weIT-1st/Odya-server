package kr.weit.odya.support

import kr.weit.odya.service.dto.TestRequest

fun createTestRequest(): TestRequest {
    return TestRequest(" testName ")
}

fun createTestEmptyRequest(): TestRequest {
    return TestRequest("")
}

fun createTestErrorRequest(): TestRequest {
    return TestRequest("김한빈")
}
