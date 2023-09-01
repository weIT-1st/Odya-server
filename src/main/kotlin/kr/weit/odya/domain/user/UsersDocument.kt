package kr.weit.odya.domain.user

import jakarta.persistence.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.util.Date

@Document(indexName = "#{@environment.getProperty('open-search.indices.users')}")
data class UsersDocument(
    @Id
    val id: Long,
    @Field(name = "nickname", type = FieldType.Text)
    val nickname: String,
    @Field(name = "@timestamp", type = FieldType.Date)
    val createdAt: Date = Date(),
) {
    constructor(user: User) : this(
        user.id,
        user.nickname,
    )
}
