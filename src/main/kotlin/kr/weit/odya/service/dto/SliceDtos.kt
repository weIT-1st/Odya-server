package kr.weit.odya.service.dto

import org.springframework.data.domain.Pageable

data class SliceResponse<T>(
    val hasNext: Boolean,
    val content: List<T>
) {
    constructor(pageable: Pageable, content: List<T>) : this(
        content.size > pageable.pageSize,
        if (content.size > pageable.pageSize) content.dropLast(1) else content
    )
}
