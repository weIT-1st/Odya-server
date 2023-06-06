package kr.weit.odya.domain.test

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

// JPA 엔티티에서 테이블 명에 해당하는게 Opensearch에서는 인덱스명입니다
// JPA에서 꼭 반드시 필요한 내용만 ES로 저장하는게 좋을것 같습니다 ㅎㅎ
// 참고로 인덱스는 제가 Opensearch에서 작업을 해야 적용이 됩니다~!
@Document(indexName = "#{@environment.getProperty('open-search.indices.test')}")
data class TestDocument(
    @Id
    val id: Long,

    @Field(type = FieldType.Text)
    val name: String
)
