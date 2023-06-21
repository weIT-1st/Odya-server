package kr.weit.odya.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import org.springframework.stereotype.Component

@Component
class FirebaseTokenHelper(
    private val firebaseAuth: FirebaseAuth
) {
    fun createFirebaseUser(uid: String) =
        runCatching {
            firebaseAuth.createUser(createUserRequest(uid))
        }
            .onFailure { throw CreateFirebaseUserException("$uid: 이미 등록된 사용자입니다") }

    fun createFirebaseCustomToken(uid: String): String =
        runCatching {
            firebaseAuth.createCustomToken(uid)
        }
            .onFailure { ex -> throw CreateTokenException(ex.message) }
            .getOrNull() ?: throw CreateTokenException("토큰 생성에 실패했습니다")

    fun getUid(idToken: String): String =
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

    private fun createUserRequest(uid: String): UserRecord.CreateRequest? =
        UserRecord.CreateRequest().setUid(uid)
}
