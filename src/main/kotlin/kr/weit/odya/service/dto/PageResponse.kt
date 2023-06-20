package kr.weit.odya.service.dto

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

data class PageResponse<T>(
    val page: Int,
    val totalPages: Int,
    val totalElements: Int,
    val content: List<T>
) {
    constructor(pageable: Pageable, page: Page<T>) : this(
        pageable.pageNumber,
        page.totalPages,
        page.totalElements.toInt(),
        page.content.toList()
    )
}
