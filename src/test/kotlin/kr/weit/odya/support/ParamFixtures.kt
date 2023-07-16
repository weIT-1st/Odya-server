package kr.weit.odya.support

import kr.weit.odya.domain.follow.FollowSortType
import org.springframework.data.domain.PageRequest

const val PAGE_PARAM = "page"
const val SIZE_PARAM = "size"
const val SORT_TYPE_PARAM = "sortType"
const val LAST_ID_PARAM = "lastId"
const val TEST_DEFAULT_PAGE = 0
const val TEST_DEFAULT_SIZE = 10
const val TEST_PAGE: Int = 1
const val TEST_SIZE: Int = 1
const val TEST_INVALID_SIZE: Int = -1
val TEST_PAGEABLE = PageRequest.of(TEST_PAGE, TEST_SIZE)
val TEST_DEFAULT_PAGEABLE = PageRequest.of(TEST_DEFAULT_PAGE, TEST_DEFAULT_SIZE)
val TEST_DEFAULT_SORT_TYPE: FollowSortType = FollowSortType.LATEST
val TEST_SORT_TYPE: FollowSortType = FollowSortType.OLDEST
