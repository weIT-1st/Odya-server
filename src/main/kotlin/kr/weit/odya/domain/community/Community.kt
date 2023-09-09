package kr.weit.odya.domain.community

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.topic.Topic
import kr.weit.odya.domain.traveljournal.TravelJournal
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseModifiableEntity

@Table(
    indexes = [
        Index(name = "community_topic_id_index", columnList = "topic_id"),
        Index(name = "community_travel_journal_id_index", columnList = "travel_journal_id"),
        Index(name = "community_user_id_index", columnList = "user_id"),
    ],
)
@Entity
@SequenceGenerator(
    name = "COMMUNITY_SEQ_GENERATOR",
    sequenceName = "COMMUNITY_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class Community(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMUNITY_SEQ_GENERATOR")
    val id: Long = 0L,

    @Column(nullable = false, length = 600)
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val visibility: CommunityVisibility,

    @Column(length = 400)
    val placeId: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", columnDefinition = "NUMERIC(19, 0)")
    val topic: Topic? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_journal_id", columnDefinition = "NUMERIC(19, 0)")
    val travelJournal: TravelJournal? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val user: User,

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "community_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val communityContentImages: List<CommunityContentImage>,
) : BaseModifiableEntity()
