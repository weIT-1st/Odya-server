package kr.weit.odya.security

import com.google.firebase.auth.FirebaseAuth
import org.springframework.stereotype.Component

@Component
class FirebaseTokenParser(
    private val firebaseAuth: FirebaseAuth
) {
    fun getUsername(idToken: String): String =
        runCatching { firebaseAuth.verifyIdToken(idToken).uid }
            .onFailure { ex -> throw IllegalArgumentException(ex.message) }
            .getOrThrow()
}
