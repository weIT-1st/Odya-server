package kr.weit.odya.domain.contentimage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.community.CommunityContentImage
import kr.weit.odya.domain.traveljournal.TravelJournalContentImage
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity
import org.locationtech.jts.geom.Point

@Table(indexes = [Index(name = "content_image_place_id_index", columnList = "place_id")])
@Entity
@SequenceGenerator(
    name = "CONTENT_IMAGE_SEQ_GENERATOR",
    sequenceName = "CONTENT_IMAGE_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class ContentImage(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CONTENT_IMAGE_SEQ_GENERATOR")
    val id: Long = 0L,

    @Column(nullable = false, updatable = false, length = 30)
    val name: String,

    @Column(nullable = false, updatable = false)
    val originName: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User,

    @OneToOne(mappedBy = "contentImage", optional = true)
    val communityContentImage: CommunityContentImage? = null,

    @OneToOne(mappedBy = "contentImage", optional = true)
    val travelJournalContentImage: TravelJournalContentImage? = null,
) : BaseTimeEntity() {

    @Column(nullable = false)
    var isLifeShot: Boolean = false
        protected set

    @Column(name = "place_id", length = 400)
    var placeId: String? = null
        protected set

    @Column(columnDefinition = "SDO_GEOMETRY")
    var coordinate: Point? = null
        protected set

    @Column(length = 45)
    var placeName: String? = null
        protected set

    fun setLifeShot() {
        isLifeShot = true
    }

    fun unsetLifeShot() {
        isLifeShot = false
        placeId = null
        coordinate = null
        placeName = null
    }

    fun setPlace(placeId: String?, coordinate: Point?, placeName: String?) {
        this.placeId = placeId
        this.coordinate = coordinate
        this.placeName = placeName
    }
}
