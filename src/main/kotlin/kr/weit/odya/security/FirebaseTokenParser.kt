package kr.weit.odya.security

import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Component

@Component
class FirebaseTokenParser(
    private val firebaseAuth: FirebaseAuth
) {
    fun getUsername(idToken: String): String =
        runCatching { firebaseAuth.verifyIdToken(idToken).uid }
            .onFailure { ex -> throw InvalidTokenException(ex.message) }
            .getOrNull() ?: throw InvalidTokenException("uid가 존재하지 않습니다")

    fun getEmail(idToken: String): String =
        runCatching {
            val uid = firebaseAuth.verifyIdToken(idToken).uid
            firebaseAuth.getUser(uid).email
        }
            .onFailure { ex -> throw InvalidTokenException(ex.message) }
            .getOrNull() ?: throw NoSuchElementException("인증된 이메일이 존재하지 않습니다")

    fun getPhoneNumber(idToken: String): String =
        runCatching {
            val uid = firebaseAuth.verifyIdToken(idToken).uid
            firebaseAuth.getUser(uid).phoneNumber
        }
            .onFailure { ex -> throw InvalidTokenException(ex.message) }
            .getOrNull() ?: throw NoSuchElementException("인증된 전화번호가 존재하지 않습니다")
}
