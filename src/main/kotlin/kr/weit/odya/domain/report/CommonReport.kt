package kr.weit.odya.domain.report

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.weit.odya.domain.user.User

@Embeddable
data class CommonReportInformation(
    @ManyToOne
    @JoinColumn(name = "user_id", columnDefinition = "NUMERIC(19, 0)", updatable = false, nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    val reportReason: ReportReason,

    @Column(updatable = false, length = 60)
    val otherReason: String? = null,
) {
    companion object {
        fun of(user: User, reportReason: ReportReason, otherReason: String?): CommonReportInformation =
            CommonReportInformation(
                user,
                reportReason,
                if (reportReason != ReportReason.OTHER) null else otherReason,
            )
    }
}
