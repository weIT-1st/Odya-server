package kr.weit.odya.domain.test

import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

// 일반적인 Repository처럼 사용할수 있지만 JSDL과 같은 고급 기능은 아쉽게도 불가능합니다 ㅠㅠ
// CrudRepository외에도 PagingAndSortingRepository도 사용가능합니다!
@Profile("!test")
@Repository
interface TestOpenSearchRepository : CrudRepository<TestDocument, Long> {
    fun findByNameLikeOrderByIdDesc(name: String): List<TestDocument>
}
