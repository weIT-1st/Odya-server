package kr.weit.odya.service

import kr.weit.odya.support.exception.ErrorCode

open class OdyaException(val errorCode: ErrorCode, message: String) : RuntimeException(message)

class ExistResourceException(message: String) : OdyaException(ErrorCode.EXIST_RESOURCE, message)

class UnRegisteredUserException(message: String) : OdyaException(ErrorCode.UNREGISTERED_USER, message)

class ObjectStorageException(message: String) : OdyaException(ErrorCode.OBJECT_STORAGE_EXCEPTION, message)

class NotFoundDefaultResourceException(message: String) :
    OdyaException(ErrorCode.NOT_FOUND_DEFAULT_RESOURCE, message)

class RedisLockFailedException(message: String) :
    OdyaException(ErrorCode.REDIS_LOCK_FAILED_EXCEPTION, message)
