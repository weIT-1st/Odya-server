package kr.weit.odya.service

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk
import kr.weit.odya.domain.placeSearchHistory.PlaceSearchHistoryRepository
import kr.weit.odya.domain.user.UserRepository
import kr.weit.odya.domain.user.getByUserId
import kr.weit.odya.support.TEST_AGE_RANGE
import kr.weit.odya.support.TEST_PLACE_SEARCH_TERM
import kr.weit.odya.support.TEST_USER_ID
import kr.weit.odya.support.createListSearchTerm
import kr.weit.odya.support.createPlaceSearchHistory
import kr.weit.odya.support.createUser
import org.opensearch.spring.boot.autoconfigure.test.DataOpenSearchTest
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@DataOpenSearchTest
@EnableElasticsearchRepositories(basePackageClasses = [PlaceSearchHistoryRepository::class])
class PlaceSearchHistoryServiceTest : DescribeSpec(
    {
        val userRepository = mockk<UserRepository>()
        val placeSearchHistoryRepository = mockk<PlaceSearchHistoryRepository>()
        val sut = PlaceSearchHistoryService(placeSearchHistoryRepository, userRepository)
        describe("saveSearchHistory 메소드") {
            context("유효한 데이터가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { placeSearchHistoryRepository.save(any()) } returns createPlaceSearchHistory()
                it("리뷰를 생성한다.") {
                    shouldNotThrow<Exception> { sut.saveSearchHistory(TEST_PLACE_SEARCH_TERM, TEST_USER_ID) }
                }
            }

            context("가입되어 있지 않은 USERID가 주어지는 경우") {
                every { userRepository.getByUserId(TEST_USER_ID) } throws NoSuchElementException()
                it("[NoSuchElementException] 예외가 발생한다") {
                    shouldThrow<NoSuchElementException> { sut.saveSearchHistory(TEST_PLACE_SEARCH_TERM, TEST_USER_ID) }
                }
            }
        }

        describe("getOverallRanking 메소드") {
            context("유효한 요청이 들어오면") {
                every { placeSearchHistoryRepository.getRecentTop10Keywords(null) } returns createListSearchTerm()
                it("검색어 전체 순위 출력") {
                    shouldNotThrow<Exception> { sut.getOverallRanking() }
                }
            }
        }

        describe("getAgeRangeRanking 메소드") {
            context("로그인한 유저와 연령대를 전달되면") {
                every { placeSearchHistoryRepository.getRecentTop10Keywords(TEST_AGE_RANGE) } returns createListSearchTerm()
                it("해당 연령대 순위 출력") {
                    shouldNotThrow<Exception> { sut.getAgeRangeRanking(TEST_USER_ID, TEST_AGE_RANGE) }
                }
            }

            context("로그인한 유저만 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { placeSearchHistoryRepository.getRecentTop10Keywords(TEST_AGE_RANGE) } returns createListSearchTerm()
                it("유저 연령대 순위 출력") {
                    shouldNotThrow<Exception> { sut.getAgeRangeRanking(TEST_USER_ID, null) }
                }
            }

            context("가입되어 있지 않은 USERID가 전달되면") {
                every { userRepository.getByUserId(TEST_USER_ID) } returns createUser()
                every { placeSearchHistoryRepository.getRecentTop10Keywords(TEST_AGE_RANGE) } returns createListSearchTerm()
                it("유저 연령대 순위 출력") {
                    shouldNotThrow<Exception> { sut.getAgeRangeRanking(TEST_USER_ID, null) }
                }
            }
        }
    },
)
