package kr.weit.odya.support

import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.service.dto.TermsContentResponse
import kr.weit.odya.service.dto.TermsTitleListResponse

const val TEST_TERMS_ID = 1L
const val TEST_OTHER_TERMS_ID = 2L
const val TEST_NOT_EXIST_TERMS_ID = 10L
const val TEST_INVALID_TERMS_ID = -1L
const val TEST_REQUIRED_TERMS_TITLE = "필수 테스트 약관"
const val TEST_REQUIRED_TERMS_CONTENT = "필수 테스트 약관 내용"
const val TEST_OPTIONAL_TERMS_TITLE = "선택 테스트 약관"
const val TEST_OPTIONAL_TERMS_CONTENT = "선택 테스트 약관 내용"

fun createRequiredTerms(id: Long = TEST_TERMS_ID) = Terms(
    id,
    title = TEST_REQUIRED_TERMS_TITLE,
    content = TEST_REQUIRED_TERMS_CONTENT,
    true,
)

fun createOptionalTerms(id: Long = TEST_OTHER_TERMS_ID) = Terms(
    id,
    title = TEST_OPTIONAL_TERMS_TITLE,
    content = TEST_OPTIONAL_TERMS_CONTENT,
    false,
)

fun createTermsList() = listOf(createRequiredTerms(), createOptionalTerms())

fun createTermsListResponse() = listOf(
    TermsTitleListResponse(createRequiredTerms()),
    TermsTitleListResponse(createOptionalTerms()),
)

fun createTermsContentResponse(terms: Terms = createRequiredTerms()) = TermsContentResponse(terms)

fun createTermsIdList() = listOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID)
