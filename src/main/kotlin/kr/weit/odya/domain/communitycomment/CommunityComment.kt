package kr.weit.odya.domain.communitycomment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseModifiableEntity

@Entity
@SequenceGenerator(
    name = "COMMUNITY_COMMENT_SEQ_GENERATOR",
    sequenceName = "COMMUNITY_COMMENT_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Table(
    indexes = [
        Index(name = "community_index", columnList = "community_id"),
    ],
)
class CommunityComment(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMUNITY_COMMENT_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val community: Community,

    content: String,
) : BaseModifiableEntity() {
    @Column(length = 300, nullable = false)
    var content: String = content
        protected set

    fun updateContent(content: String) {
        this.content = content
    }
}
