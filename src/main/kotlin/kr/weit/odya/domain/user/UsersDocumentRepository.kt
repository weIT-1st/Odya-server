package kr.weit.odya.domain.user

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

fun UsersDocumentRepository.getByNickname(nickname: String): List<UsersDocument> =
    findByNicknameContainingIgnoreCaseOrderByIdDesc(nickname)

interface UsersDocumentRepository : ElasticsearchRepository<UsersDocument, Long> {
    fun findByNicknameContainingIgnoreCaseOrderByIdDesc(nickname: String): List<UsersDocument>
}
