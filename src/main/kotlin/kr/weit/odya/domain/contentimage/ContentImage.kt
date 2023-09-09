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
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(
    indexes = [
        Index(name = "content_image_user_id_index", columnList = "user_id"),
    ],
)
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

    @Column(nullable = false)
    val isLifeShot: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    val user: User,
) : BaseTimeEntity()
