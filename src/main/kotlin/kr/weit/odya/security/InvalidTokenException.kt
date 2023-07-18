package kr.weit.odya.security

import kr.weit.odya.support.exception.ErrorCode

open class FirebaseAuthException(val errorCode: ErrorCode, message: String? = null) : RuntimeException(message)

class InvalidTokenException(message: String? = null) :
    FirebaseAuthException(ErrorCode.INVALID_FIREBASE_ID_TOKEN, message)

class CreateFirebaseUserException(message: String? = null) :
    FirebaseAuthException(ErrorCode.FIREBASE_USER_CREATION_FAIL, message)

class CreateFirebaseCustomTokenException(message: String? = null) :
    FirebaseAuthException(ErrorCode.FIREBASE_CUSTOM_TOKEN_CREATION_FAIL, message)
