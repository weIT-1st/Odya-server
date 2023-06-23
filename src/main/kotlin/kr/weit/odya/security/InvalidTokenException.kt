package kr.weit.odya.security

open class FirebaseAuthException(message: String? = null) : RuntimeException(message)

class InvalidTokenException(message: String? = null) : FirebaseAuthException(message)

class CreateFirebaseUserException(message: String? = null) : FirebaseAuthException(message)

class CreateTokenException(message: String? = null) : FirebaseAuthException(message)
