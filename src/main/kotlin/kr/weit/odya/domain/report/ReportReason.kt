package kr.weit.odya.domain.report

enum class ReportReason(val reason: String) {
    SPAM("스팸 및 홍보글"),
    PORNOGRAPHY("음란성이 포함된 글"),
    SWEAR_WORD("욕설/생명경시/혐오/차별적인 글"),
    OVER_POST("게시글 도배"),
    INFO_LEAK("개인정보 노출 및 불법 정보"),
    OTHER("기타"),
}
