package kr.weit.odya.support

import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.domain.user.User
import kr.weit.odya.service.dto.ModifyAgreedTermsRequest
import kr.weit.odya.service.dto.OptionalAgreedTermsResponse
import kr.weit.odya.service.dto.TermsContentResponse
import kr.weit.odya.service.dto.TermsTitleListResponse
import kr.weit.odya.service.dto.TermsUpdateResponse

const val TEST_TERMS_ID = 1L
const val TEST_OTHER_TERMS_ID = 2L
const val TEST_OTHER_TERMS_ID_2 = 3L
const val TEST_NOT_EXIST_TERMS_ID = 9999L
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
    true,
)

fun createOptionalTerms(id: Long = TEST_OTHER_TERMS_ID, title: String = TEST_OPTIONAL_TERMS_TITLE) = Terms(
    id,
    title,
    content = TEST_OPTIONAL_TERMS_CONTENT,
    false,
)

fun createTermsList() = listOf(createRequiredTerms(), createOptionalTerms(), createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2))

fun createTermsListResponse() = listOf(
    TermsTitleListResponse(createRequiredTerms()),
    TermsTitleListResponse(createOptionalTerms()),
    TermsTitleListResponse(createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2)),
)
fun createTermsContentResponse(terms: Terms = createRequiredTerms()) = TermsContentResponse(terms)

fun createTermsIdList() = setOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID, TEST_OTHER_TERMS_ID_2)

fun createRequiredTermsIdList() = listOf(
    TEST_TERMS_ID,
    TEST_OTHER_TERMS_ID_2,
)

fun createOptionalTermsList() = listOf(createOptionalTerms(), createOptionalTerms(4L, "선택 테스트 약관2"))

fun createModifyAgreedTermsResponse(user: User) = TermsUpdateResponse(createOptionalTermsList().map { TermsTitleListResponse(it) }, listOf(createAgreedTerms(user, TEST_OTHER_AGREED_TERMS_ID, createOptionalTerms())).map { OptionalAgreedTermsResponse(it) })

fun createModifyAgreedTermsRequest() = ModifyAgreedTermsRequest(setOf(TEST_TERMS_ID, TEST_OTHER_TERMS_ID_2), setOf(TEST_OTHER_TERMS_ID))
