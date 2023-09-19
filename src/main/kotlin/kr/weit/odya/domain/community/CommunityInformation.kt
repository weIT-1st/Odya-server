package kr.weit.odya.domain.community

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class CommunityInformation(
    @Column(nullable = false, length = 600)
    val content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val visibility: CommunityVisibility,

    @Column(length = 400)
    val placeId: String? = null,
)
