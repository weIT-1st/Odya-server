package kr.weit.odya.support.exception

import org.springframework.http.HttpStatus

/**
 * -11xxx: Firebase 관련 에러코드
 * -12xxx: Object Storage 관련 에러코드
 * -13xxx: Client 관련 에러코드
 */
enum class ErrorCode(val httpStatus: HttpStatus, val code: Int, val errorMessage: String) {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, -10000, "Invalid request"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, -10001, "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, -10002, "Forbidden"),
    NO_SUCH_ELEMENT(HttpStatus.NOT_FOUND, -10003, "No such element"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, -10004, "Internal server error"),
    UNREGISTERED_USER(HttpStatus.UNAUTHORIZED, -10005, "Unregistered user"),
    EXIST_RESOURCE(HttpStatus.CONFLICT, -10006, "Exist resource"),
    NOT_FOUND_DEFAULT_RESOURCE(HttpStatus.INTERNAL_SERVER_ERROR, -10007, "Not found default resource"),
    INVALID_FIREBASE_ID_TOKEN(HttpStatus.UNAUTHORIZED, -11000, "Invalid Firebase ID token"),
    FIREBASE_USER_CREATION_FAIL(HttpStatus.CONFLICT, -11001, "Firebase user creation fail"),
    FIREBASE_CUSTOM_TOKEN_CREATION_FAIL(
        HttpStatus.INTERNAL_SERVER_ERROR,
        -11002,
        "Firebase custom token creation fail",
    ),
    FIREBASE_USER_WITHDRAW_FAIL(HttpStatus.NOT_FOUND, -11003, "Firebase user withdraw fail(USER_NOT_FOUND)"),
    OBJECT_STORAGE_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, -12000, "Object storage exception"),
    KAKAO_CLIENT_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, -13000, "Kakao client exception"),
    REDIS_LOCK_FAILED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, -14000, "Failed to get Redis lock"),
}
