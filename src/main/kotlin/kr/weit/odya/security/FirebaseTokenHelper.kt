package kr.weit.odya.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserRecord
import kr.weit.odya.util.getOrThrow
import org.springframework.stereotype.Component

@Component
class FirebaseTokenHelper(
    private val firebaseAuth: FirebaseAuth,
) {
    fun createFirebaseUser(username: String) {
        try {
            firebaseAuth.createUser(createUserRequest(username))
        } catch (ex: FirebaseAuthException) {
            throw CreateFirebaseUserException("$username: 이미 등록된 사용자입니다")
        }
    }

    fun createFirebaseCustomToken(username: String): String =
        runCatching {
            firebaseAuth.createCustomToken(username)
        }
            .getOrThrow { ex -> throw CreateFirebaseCustomTokenException(ex.message) }

    fun getUid(idToken: String): String =
        runCatching { firebaseAuth.verifyIdToken(idToken).uid }
            .getOrThrow { ex -> throw InvalidTokenException(ex.message) }

    fun getEmail(idToken: String): String =
        runCatching {
            val uid = firebaseAuth.verifyIdToken(idToken).uid
            firebaseAuth.getUser(uid).email
        }
            .getOrThrow { ex -> throw InvalidTokenException(ex.message) }

    fun getPhoneNumber(idToken: String): String =
        runCatching {
            val uid = firebaseAuth.verifyIdToken(idToken).uid
            firebaseAuth.getUser(uid).phoneNumber
        }
            .getOrThrow { ex -> throw InvalidTokenException(ex.message) }

    private fun createUserRequest(username: String): UserRecord.CreateRequest? =
        UserRecord.CreateRequest().setUid(username)
}
