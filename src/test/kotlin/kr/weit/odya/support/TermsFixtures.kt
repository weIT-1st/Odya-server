package kr.weit.odya.support

import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.service.dto.TermsContentResponse
import kr.weit.odya.service.dto.TermsTitleListResponse
import kr.weit.odya.service.dto.TermsUpdateRequest

const val TEST_TERMS_ID = 1L
const val TEST_OTHER_TERMS_ID = 2L
const val TEST_OTHER_TERMS_ID_2 = 3L
const val TEST_NOT_EXIST_TERMS_ID = 10L
const val TEST_INVALID_TERMS_ID = -1L
const val TEST_REQUIRED_TERMS_TITLE = "필수 테스트 약관"
const val TEST_REQUIRED_TERMS_TITLE_2 = "필수 테스트 약관2"
const val TEST_REQUIRED_TERMS_CONTENT = "필수 테스트 약관 내용"
const val TEST_OPTIONAL_TERMS_TITLE = "선택 테스트 약관"
const val TEST_OPTIONAL_TERMS_CONTENT = "선택 테스트 약관 내용"

fun createRequiredTerms(id: Long = TEST_TERMS_ID, title: String = TEST_REQUIRED_TERMS_TITLE) = Terms(
    id,
    title,
    content = TEST_REQUIRED_TERMS_CONTENT,
    1,
)

fun createOptionalTerms(id: Long = TEST_OTHER_TERMS_ID) = Terms(
    id,
    title = TEST_OPTIONAL_TERMS_TITLE,
    content = TEST_OPTIONAL_TERMS_CONTENT,
    0,
)

fun createTermsList() = listOf(createRequiredTerms(), createOptionalTerms(), createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2))

fun createTermsListResponse() = listOf(
    TermsTitleListResponse(createRequiredTerms()),
    TermsTitleListResponse(createOptionalTerms()),
    TermsTitleListResponse(createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2)),
)

fun createTermsContentResponse(terms: Terms = createRequiredTerms()) = TermsContentResponse(terms)

fun createTermsIdList() = listOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID, TEST_OTHER_TERMS_ID_2)

fun createRequiredTermsList() = listOf(
    createRequiredTerms(),
    createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2),
)

fun createOptionalTermsList() = listOf(createOptionalTerms())

fun createTermsUpdateRequest() = TermsUpdateRequest(setOf(4L, 5L), setOf(TEST_OTHER_TERMS_ID_2, 6L))
