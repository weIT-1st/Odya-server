package kr.weit.odya.domain.community

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
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

private const val MIN_COMMUNITY_CONTENT_IMAGE_COUNT = 1

@Table(
    indexes = [
        Index(name = "community_topic_id_index", columnList = "topic_id"),
        Index(name = "community_travel_journal_id_index", columnList = "travel_journal_id"),
        Index(name = "community_user_id_index", columnList = "user_id"),
        Index(name = "community_place_id_index", columnList = "placeId"),
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    val user: User,

    topic: Topic? = null,

    travelJournal: TravelJournal? = null,

    communityContentImages: List<CommunityContentImage>,

    communityInformation: CommunityInformation,
) : BaseModifiableEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", columnDefinition = "NUMERIC(19, 0)")
    var topic: Topic? = topic
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_journal_id", columnDefinition = "NUMERIC(19, 0)")
    var travelJournal: TravelJournal? = travelJournal
        protected set

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE], orphanRemoval = true)
    @JoinColumn(name = "community_id", nullable = false, updatable = false, columnDefinition = "NUMERIC(19, 0)")
    private val mutableCommunityContentImages: MutableList<CommunityContentImage> =
        communityContentImages.toMutableList()
    val communityContentImages: List<CommunityContentImage>
        get() = mutableCommunityContentImages

    @Embedded
    var communityInformation: CommunityInformation = communityInformation
        protected set

    val content: String
        get() = communityInformation.content

    val visibility: CommunityVisibility
        get() = communityInformation.visibility

    val placeId: String?
        get() = communityInformation.placeId

    fun updateCommunity(communityInformation: CommunityInformation, travelJournal: TravelJournal?, topic: Topic?) {
        this.communityInformation = communityInformation
        this.travelJournal = travelJournal
        this.topic = topic
    }

    fun addCommunityContentImages(communityContentImages: List<CommunityContentImage>) {
        mutableCommunityContentImages.addAll(communityContentImages)
    }

    fun deleteCommunityContentImages(communityContentImages: List<CommunityContentImage>) {
        if (mutableCommunityContentImages.size - communityContentImages.size < MIN_COMMUNITY_CONTENT_IMAGE_COUNT) {
            throw IllegalArgumentException("커뮤니티 컨텐츠 이미지는 최소 $MIN_COMMUNITY_CONTENT_IMAGE_COUNT 개 이상이어야 합니다.")
        }
        mutableCommunityContentImages.removeAll(communityContentImages)
    }
}
