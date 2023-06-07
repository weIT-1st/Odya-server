package kr.weit.odya.service

open class OdyaException(message: String) : RuntimeException(message)

class ExistResourceException(message: String) : OdyaException(message)

class LoginFailedException(message: String) : OdyaException(message)
