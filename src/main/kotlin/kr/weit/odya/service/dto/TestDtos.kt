package kr.weit.odya.service.dto

data class TestRequest(val name: String)

data class TestResponse(val hashValue: Int, val originalName: String)
