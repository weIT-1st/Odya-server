package kr.weit.odya.service

open class OdyaException(message: String) : RuntimeException(message)

class ExistResourceException(message: String) : OdyaException(message)

class NotExistResourceException(message: String) : OdyaException(message)
