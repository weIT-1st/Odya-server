package kr.weit.odya.support

import kr.weit.odya.domain.agreedTerms.AgreedTerms
import kr.weit.odya.domain.terms.Terms
import kr.weit.odya.domain.user.User

const val TEST_AGREED_TERMS_ID = 1L
const val TEST_OTHER_AGREED_TERMS_ID = 2L
const val TEST_OTHER_AGREED_TERMS_ID_2 = 3L

fun createAgreedTerms(user: User = createUser(), id: Long = TEST_AGREED_TERMS_ID, terms: Terms = createRequiredTerms()) =
    AgreedTerms(id, user, terms)

fun createAgreedTermsList(user: User = createUser()) = listOf(
    createAgreedTerms(),
    createAgreedTerms(user, TEST_OTHER_AGREED_TERMS_ID, createOptionalTerms()),
    createAgreedTerms(user, TEST_OTHER_AGREED_TERMS_ID_2, createRequiredTerms(TEST_OTHER_TERMS_ID_2, TEST_REQUIRED_TERMS_TITLE_2)),
)

fun createOptionalAgreedTermsList(user: User = createUser()) = listOf(createAgreedTerms(user, TEST_OTHER_AGREED_TERMS_ID, createRequiredTerms(TEST_OTHER_TERMS_ID, TEST_OPTIONAL_TERMS_TITLE)))
