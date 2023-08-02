package kr.weit.odya.domain.favoriteTopic

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.user.User

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "FavoriteTopic_unique",
            columnNames = ["user_id", "topic_id"],
        ),
    ],
)
@SequenceGenerator(
    name = "FAVORITE_TOPIC_SEQ_GENERATOR",
    sequenceName = "FAVORITE_TOPIC_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
@Entity
class FavoriteTopic(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FAVORITE_TOPIC_SEQ_GENERATOR")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "topic_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val topic: Topic,
) {
    val registrantsId: Long
        get() = user.id
}
