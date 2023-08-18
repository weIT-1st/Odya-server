package kr.weit.odya.domain.traveljournal

enum class TravelJournalVisibility(val description: String) {
    PUBLIC("모두 공개"), FRIEND_ONLY("친구에게만 공개"), PRIVATE("비공개")
}
