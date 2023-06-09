package kr.weit.odya.domain.user

enum class SocialType(val description: String) {
    KAKAO("카카오"), APPLE("애플");

    companion object {
        fun getValue(value: String): SocialType {
            return values().firstOrNull { enumVal -> enumVal.name == value.uppercase() }
                ?: throw IllegalArgumentException("$value: 존재하지 않는 소셜 타입입니다")
        }
    }
}
