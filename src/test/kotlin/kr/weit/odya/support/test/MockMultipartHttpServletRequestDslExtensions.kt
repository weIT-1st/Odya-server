package kr.weit.odya.support.test

import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMultipartHttpServletRequestDsl

fun MockMultipartHttpServletRequestDsl.files(size: Int, mockMultipartFile: MockMultipartFile) {
    repeat(size) {
        file(mockMultipartFile)
    }
}
