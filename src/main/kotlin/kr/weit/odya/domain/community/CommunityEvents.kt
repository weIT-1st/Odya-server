package kr.weit.odya.domain.community

data class CommunityDeleteEvent(
    val deletedCommunityContentImageNames: List<String>,
)
