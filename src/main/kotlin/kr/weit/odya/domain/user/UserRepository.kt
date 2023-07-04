package kr.weit.odya.domain.user

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull

fun UserRepository.existsByNickname(nickname: String): Boolean = existsByInformationNickname(nickname)

fun UserRepository.existsByEmail(email: String): Boolean = existsByInformationEmail(email)

fun UserRepository.existsByPhoneNumber(phoneNumber: String): Boolean = existsByInformationPhoneNumber(phoneNumber)

fun UserRepository.getByUsername(username: String): User =
    findByUsername(username) ?: throw NoSuchElementException("$username: 사용자가 존재하지 않습니다")

fun UserRepository.getByUserId(userId: Long): User =
    findByIdOrNull(userId) ?: throw NoSuchElementException("$userId: 사용자가 존재하지 않습니다")

fun UserRepository.getByUserIdWithProfile(userId: Long): User =
    findUserWithProfileById(userId) ?: throw NoSuchElementException("$userId: 사용자가 존재하지 않습니다")

interface UserRepository : JpaRepository<User, Long> {
    fun existsByUsername(username: String): Boolean

    fun existsByInformationNickname(nickname: String): Boolean

    fun findByUsername(username: String): User?

    fun existsByInformationPhoneNumber(phoneNumber: String): Boolean

    fun existsByInformationEmail(email: String): Boolean

    @EntityGraph(attributePaths = ["profile", "profile.profileColor"])
    fun findUserWithProfileById(userId: Long): User?
}
