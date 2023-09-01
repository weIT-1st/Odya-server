package kr.weit.odya.domain.community

enum class CommunityVisibility(val description: String) {
    PUBLIC("모두 공개"), FRIEND_ONLY("친구에게만 공개")
}
