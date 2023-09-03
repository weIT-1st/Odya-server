package kr.weit.odya.domain.report

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.weit.odya.domain.community.Community
import kr.weit.odya.domain.user.User
import kr.weit.odya.support.domain.BaseTimeEntity

@Table(uniqueConstraints = [UniqueConstraint(name = "report_community_unique", columnNames = ["community_id", "user_id"]) ])
@Entity
@SequenceGenerator(
    name = "REPORT_COMMUNITY_SEQ_GENERATOR",
    sequenceName = "REPORT_COMMUNITY_SEQ",
    initialValue = 1,
    allocationSize = 1,
)
class ReportCommunity(
    @Id
    @Column(columnDefinition = "NUMERIC(19, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REPORT_COMMUNITY_SEQ_GENERATOR")
    val id: Long = 0L,

    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(name = "community_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val community: Community,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    val reportReason: ReportReason,

    @Column(updatable = false, length = 60)
    val otherReason: String? = null,
) : BaseTimeEntity()
