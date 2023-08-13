package kr.weit.odya.service.dto

import kr.weit.odya.domain.terms.Terms

data class TermsTitleListResponse(
    val id: Long,
    val title: String,
    val required: Boolean,
) {
    constructor(terms: Terms) : this(
        terms.id,
        terms.title,
        terms.required,
    )
}

data class TermsContentResponse(
    val id: Long,
    val content: String,
) {
    constructor(terms: Terms) : this(
        terms.id,
        terms.content,
    )
}
