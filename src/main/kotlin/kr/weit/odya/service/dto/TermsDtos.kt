package kr.weit.odya.service.dto

import kr.weit.odya.domain.agreedTerms.AgreedTerms
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

data class OptionalAgreedTermsResponse(
    val id: Long,
    val userId: Long,
    val termsId: Long,
    val required: Boolean,
) {
    constructor(agreedTerms: AgreedTerms) : this(
        agreedTerms.id,
        agreedTerms.user.id,
        agreedTerms.terms.id,
        agreedTerms.terms.required,
    )
}

data class TermsUpdateResponse(
    val optionalAgreedTermsList: List<TermsTitleListResponse>,
    val userOptionalAgreedTermsList: List<OptionalAgreedTermsResponse>,
)

data class ModifyAgreedTermsRequest(
    val agreedTermsIdList: Set<Long>?,
    val disagreeTermsIdList: Set<Long>?,
)
