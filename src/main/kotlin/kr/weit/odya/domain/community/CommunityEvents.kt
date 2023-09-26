package kr.weit.odya.domain.community

data class CommunityUpdateEvent(
    val deletedCommunityContentImageNames: List<String>,
)

data class CommunityDeleteEvent(
    val deletedCommunityContentImageNames: List<String>,
)
